package bearmaps.utils.graph;

import bearmaps.utils.pq.MinHeapPQ;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private List<Vertex> solution;
    private int numStatesExplored;
    private double explorationTime;
    private MinHeapPQ<Vertex> PQ;
    private HashMap<Vertex, Double> distTo;
    private int status;
    private Vertex end;
    private HashMap<Vertex, Vertex> vertexToPredecessor;


    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        vertexToPredecessor = new HashMap<>();
        numStatesExplored = 0;
        this.end = end;
        timeout = timeout * 1000;
        double startTime = System.currentTimeMillis();
        PQ = new MinHeapPQ<>();
        distTo = new HashMap<>();
        solution = new LinkedList<>();
        PQ.insert(start, input.estimatedDistanceToGoal(start, end));
        distTo.put(start, (double) 0);
        status = 0;

        while (true) {
            if (PQ.size() == 0) {
                status = 2;
                break;
            }
            if (PQ.peek().equals(end)) {
                break;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                status = 1;
                break;
            }
            Vertex p = PQ.poll();
            numStatesExplored++;
            for (WeightedEdge<Vertex> e : input.neighbors(p)) {
                relaxHelper(e, input, end);
            }
        }
        explorationTime = (System.currentTimeMillis() - startTime) / 1000;
        if (status == 0) {
            solution.add(end);
            Vertex predecessor = vertexToPredecessor.get(end);
            while (!predecessor.equals(start)) {
                solution.add(0, predecessor);
                predecessor = vertexToPredecessor.get(predecessor);
            }
            solution.add(0, predecessor);
        }

    }

    private void relaxHelper(WeightedEdge<Vertex> e, AStarGraph<Vertex> input, Vertex end) {
        Vertex p = e.from();
        Vertex q = e.to();
        double w = e.weight();
        if (!distTo.containsKey(q) || distTo.get(p) + w < distTo.get(q)) {
            distTo.put(q, distTo.get(p) + w);
            vertexToPredecessor.put(q, p);
            if (PQ.contains(q)) {
                PQ.changePriority(q, distTo.get(q) + input.estimatedDistanceToGoal(q, end));
            }
            else {
                PQ.insert(q, distTo.get(q) + input.estimatedDistanceToGoal(q, end));
            }
        }

    }


    @Override
    public SolverOutcome outcome() {
        if (status == 0) {
           return SolverOutcome.SOLVED;
        }
        if (status == 1) {
            return SolverOutcome.TIMEOUT;
        }
        return SolverOutcome.UNSOLVABLE;
    }

    @Override
    public List<Vertex> solution() {
        if (status != 0) {
            return new LinkedList<>();
        }
        return solution;
    }

    @Override
    public double solutionWeight() {
        if (status != 0) {
            return 0;
        }
        return distTo.get(this.end);
    }

    @Override
    public int numStatesExplored() {
        return numStatesExplored;
    }

    @Override
    public double explorationTime() {
        return explorationTime;
    }
}
