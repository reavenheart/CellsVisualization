package objects;

/**
 * Created by Denis on 26.04.2015.
 */
public class SpaceObject3D {
    public float x;
    public float y;
    public float z;

    public SpaceObject3D() {
        x = 0;
        y = 0;
        z = 0;
    }

    public SpaceObject3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean positionEquals(SpaceObject3D edgeCenter) {

        if (x == edgeCenter.x && y == edgeCenter.y && z == edgeCenter.z)
            return true;

        return false;
    }
}
