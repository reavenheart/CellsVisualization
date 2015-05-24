package objects;

import java.util.ArrayList;

/**
 * Created by Denis on 26.04.2015.
 */
public class Vertex3D extends SpaceObject3D{

    public SpaceObject3D normal;

    public Vertex3D() {
        super();
        normal = new SpaceObject3D();
    }

    public Vertex3D(float x, float y, float z) {
        super(x,y,z);
        normal = new SpaceObject3D();
    }

    public void countNormal(ArrayList<SpaceObject3D> faceNormals) {
        for (SpaceObject3D faceNormal : faceNormals) {
            normal.x += faceNormal.x;
            normal.y += faceNormal.y;
            normal.z += faceNormal.z;
        }

        float vectorLength = (float)Math.sqrt(normal.x * normal.x + normal.y * normal.y + normal.z * normal.z);

        normal.x = normal.x/ vectorLength;
        normal.y = normal.y/ vectorLength;
        normal.z = normal.z/ vectorLength;
    }
}
