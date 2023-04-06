package bearmaps;

import bearmaps.utils.Constants;
import bearmaps.utils.graph.streetmap.StreetMapGraph;
import bearmaps.utils.graph.streetmap.Node;
import bearmaps.utils.ps.KDTree;
import bearmaps.utils.ps.NaivePointSet;
import bearmaps.utils.ps.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {
    HashMap<Point, Node> PointToNode;
    KDTree kdt;
    NaivePointSet nps;
    List<Node> originalNodes;
    Trie search;
    HashMap<String, LinkedList<Node>> cleanToNode;


    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        PointToNode = new HashMap<>();
        List<Node> nodes = this.getNodes().stream().filter(x -> neighbors(x.id()).size() != 0)
                .collect(Collectors.toList());
        List<Point> points = nodes.stream()
                .map(x -> new Point(projectToX(x.lon(), x.lat()), projectToY(x.lon(), x.lat())))
                .collect(Collectors.toList());
        for (int i = 0; i < nodes.size(); i++) {
            PointToNode.put(points.get(i), nodes.get(i));
        }
        kdt = new KDTree(points);
        originalNodes = this.getAllNodes();
        search = new Trie();
        cleanToNode = new HashMap<>();
        for (Node node : originalNodes) {
            if (node.name() != null) {
                String cleanName = cleanString(node.name());
                search.add(cleanName);
                if (cleanToNode.get(cleanName) == null) {
                    cleanToNode.put(cleanName, new LinkedList<>());
                }
                cleanToNode.get(cleanName).add(node);
            }

        }

        //nps = new NaivePointSet(points);
    }


    /**
     * For Project Part III
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        double x = projectToX(lon, lat);
        double y = projectToY(lon, lat);
        Point nearest = kdt.nearest(x, y);
        //Point nearest = nps.nearest(x, y);
        return PointToNode.get(nearest).id();
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }


    /**
     * For Project Part IV (extra credit)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        prefix = cleanString(prefix);
        List<String> cleanedNames = search.keysWithPrefix(prefix);
        List<String> result = new ArrayList<>();
        for (String name : cleanedNames) {
            for (Node n : cleanToNode.get(name)) {
                result.add(n.name());
            }
        }

        return result;
    }


    /**
     * For Project Part IV (extra credit)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        locationName = cleanString(locationName);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Node n : cleanToNode.get(locationName)) {
            Map<String, Object> map = new HashMap<>();
            map.put("lat", n.lat());
            map.put("lon", n.lon());
            map.put("name", n.name());
            map.put("id", n.id());
            result.add(map);
        }
        return result;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     * Make this private
     */
    public static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

        
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;
    /** Latitude centered on Berkeley. */
    private static final double ROOT_LAT = (Constants.ROOT_ULLAT + Constants.ROOT_LRLAT) / 2;
    /** Longitude centered on Berkeley. */
    private static final double ROOT_LON = (Constants.ROOT_ULLON + Constants.ROOT_LRLON) / 2;



    private class Trie {
        TrieNode root;

        public Trie() {
            root = new TrieNode('c');
        }

        public Trie(TrieNode root) {
            this.root = root;
        }

        public void clear() {
            root.map = new HashMap<>();
            root.end = false;
        }


        public boolean contains(String key) {
            if (key.equals("")) {
                if (root.end) {
                    return true;
                } else {
                    return false;
                }
            }
            char c = key.charAt(0);
            if (root.map.containsKey(c)) {
                return (new Trie(root.map.get(c))).contains(key.substring(1));
            } else {
                return false;
            }
        }

        public void add(String key) {
            if (key == null || key.length() < 1) {
                return;
            }
            TrieNode curr = root;
            for (int i = 0, n = key.length(); i < n; i++) {
                char c = key.charAt(i);
                if (!curr.map.containsKey(c)) {
                    curr.map.put(c, new TrieNode(c, false));
                }
                curr = curr.map.get(c);
            }
            curr.end = true;
        }


        public List<String> keysWithPrefix(String prefix) {
            ArrayList<String> rList = new ArrayList<>();
            TrieNode n = root;
            for (char c : prefix.toCharArray()) {
                if (n.map.containsKey(c)) {
                    n = n.map.get(c);
                } else {
                    return rList;
                }
            }
            if (n.end) {
                rList.add(prefix);
            }
            for (char c : n.map.keySet()) {
                (new Trie(n.map.get(c))).prefixHelper(rList, prefix, "");
            }
            return rList;
        }

        public void prefixHelper(ArrayList<String> rList, String prefix, String soFar) {
            soFar = soFar + root.item;
            if (root.end) {
                rList.add(prefix + soFar);
            }
            if (root.map.isEmpty()) {
                return;
            }
            for (char c : root.map.keySet()) {
                (new Trie(root.map.get(c))).prefixHelper(rList, prefix, soFar);
            }
        }




        public String longestPrefixOf(String key) {
            throw new UnsupportedOperationException();
        }

        class TrieNode {
            char item;
            Boolean end;
            HashMap<Character, TrieNode> map;

            public TrieNode(Character item) {
                this.item = item;
                this.map = new HashMap<>();
                this.end = false;
            }

            public TrieNode(Character item, Boolean isEnd) {
                this.item = item;
                this.map = new HashMap<>();
                this.end = isEnd;
            }



        }



    }

}
