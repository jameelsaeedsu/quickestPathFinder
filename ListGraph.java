import java.util.*;

public class ListGraph<T> implements Graph<T>{

    private final Map<T, Set<Edge<T>>> nodes = new HashMap<>();

    private void catchNoSuchNode(T node){
        if(!nodes.containsKey(node))
            throw new NoSuchElementException();
    }

    @Override
    public void add(T node) {
        nodes.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public void connect(T node1, T node2, String name, int weight) {
        if(weight<0)
            throw new IllegalArgumentException();
        if(getEdgeBetween(node1, node2) != null)
            throw new IllegalStateException();

        nodes.get(node1).add(new Edge<>(node2, name, weight));
        nodes.get(node2).add(new Edge<>(node1, name, weight));
    }

    @Override
    public void setConnectionWeight(T node1, T node2, int weight) {
        if(weight<0)
            throw new IllegalArgumentException();
        getEdgeBetween(node1, node2).setWeight(weight);
        getEdgeBetween(node2, node1).setWeight(weight);
    }

    @Override
    public Set<T> getNodes() {
        return Collections.unmodifiableSet(nodes.keySet());
    }

    @Override
    public Collection<Edge<T>> getEdgesFrom(T node) {
        return Collections.unmodifiableCollection(nodes.get(node));
    }

    @Override
    public Edge<T> getEdgeBetween(T node1, T node2) {
        catchNoSuchNode(node1);
        catchNoSuchNode(node2);

        for (Edge<T> e : nodes.get(node1))
            if (e.getDestination().equals(node2))
                return e;

        return null;
    }

    @Override
    public void disconnect(T node1, T node2) {
        catchNoSuchNode(node1);
        catchNoSuchNode(node2);

        if(getEdgeBetween(node1, node2) == null)
            throw new IllegalStateException();

        nodes.get(node1).removeIf(e -> e.getDestination().equals(node2));
        nodes.get(node2).removeIf(e -> e.getDestination().equals(node1));
    }

    @Override
    public void remove(T node) {
        catchNoSuchNode(node);

        for (Edge<T> d : nodes.get(node))
            nodes.get(d.getDestination()).removeIf(e->e.getDestination().equals(node));

        nodes.keySet().removeIf(n->n.equals(node));
    }

    public void removeAll() {
        nodes.clear();
    }

    @Override
    public boolean pathExists(T from, T to) {
        Set<T> visited = new HashSet<>();
        depthFirstSearch(from, visited);
        return visited.contains(to);
    }

    private void depthFirstSearch(T next , Set<T> visited) {
        visited.add(next);
        for (Edge<T> e : nodes.get(next)){
            if (!visited.contains(e.getDestination()))
                depthFirstSearch(e.getDestination(), visited);
        }
    }

    private List<Edge<T>> gatherPath(T from, T to, Map<T, T> path) {
        List<Edge<T>> result = new ArrayList<>();
        T current = to;
        while(!current.equals(from)) {
            T next = path.get(current);
            Edge<T> edge = getEdgeBetween(next, current);
            result.add(edge);
            current = next;
        }
        Collections.reverse(result);
        return result;
    }

//    private List<Edge<T>> getAnyPath(T from, T to){
//        return null;
//    }

    private List<Edge<T>> getShortestPath(T from, T to) {
        Set<T> visited = new HashSet<>();
        LinkedList<T> queue = new LinkedList<>();
        Map<T, T> path = new HashMap<>();
        visited.add(from);
        queue.add(from);

        while(!queue.isEmpty()) {
            T current = queue.pollFirst();
            for (Edge<T> e : nodes.get(current)){
                T next = e.getDestination();
                if (!visited.contains(next)) {
                    queue.add(next);
                    visited.add(next);
                    path.put(next, current);
                }
            }
        }

        if (!visited.contains(to))
            return null;
        else
            return gatherPath(from, to, path);
    }

//    private List<Edge<T>> getFastestPath(T from, T to) {
//        return null;
//    }

    @Override
    public List<Edge<T>> getPath(T from, T to) {
        return getShortestPath(from, to);
    }

    @Override
    public String toString(){
        return nodes.toString();
    }

}
