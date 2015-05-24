package objects;

/**
 * Created by Denis on 24.05.2015.
 */
public class Edge3D {
    public Vertex3D v1;
    public Vertex3D v2;

    public SpaceObject3D edgeCenter;

    public Edge3D() {
        v1 = new Vertex3D();
        v2 = new Vertex3D();

        edgeCenter = new SpaceObject3D();
    }

    public Edge3D(Vertex3D v1, Vertex3D v2) {
        this.v1 = v1;
        this.v2 = v2;

        countEdgeCenter();
    }

    private void countEdgeCenter() {
        edgeCenter = new SpaceObject3D();

        float distance = (float)Math.sqrt(
                (v2.x - v1.x)*(v2.x - v1.x) +
                (v2.y - v1.y)*(v2.y - v1.y) +
                (v2.z - v1.z)*(v2.z - v1.z)
        );

        edgeCenter.x = (v2.x - v1.x)/distance;
        edgeCenter.y = (v2.y - v1.y)/distance;
        edgeCenter.z = (v2.z - v1.z)/distance;

        edgeCenter.x = (v2.x - edgeCenter.x)*distance/2;
        edgeCenter.y = (v2.y - edgeCenter.y)*distance/2;
        edgeCenter.z = (v2.z - edgeCenter.z)*distance/2;
    }

    public boolean containsVertex(Vertex3D vertex) {

        if (v1.equals(vertex) || v2.equals(vertex))
            return true;

        return false;
    }

    public boolean containsVerticies(Vertex3D v1, Vertex3D v2) {

        if ((this.v1.equals(v1) && this.v2.equals(v2)) || (this.v1.equals(v2) && this.v2.equals(v1)))
            return true;

        return false;
    }
}
