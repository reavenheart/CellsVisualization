package objects;

import com.jogamp.opengl.util.texture.TextureCoords;

import com.jogamp.opengl.GL2;

/**
 * Created by Denis on 26.04.2015.
 */
public class Polygon3D {
    public Vertex3D[] verticies;

    public SpaceObject3D polygonNormal;

    public Polygon3D() {
        verticies = new Vertex3D[4];

        for (int i = 0; i < verticies.length; i++)
            verticies[i] = new Vertex3D();

        polygonNormal = new SpaceObject3D();
    }

    public Polygon3D(Vertex3D vertex1, Vertex3D vertex2, Vertex3D vertex3, Vertex3D vertex4) {
        verticies = new Vertex3D[4];

        polygonNormal = new SpaceObject3D();

        verticies[0] = vertex1;
        verticies[1] = vertex2;
        verticies[2] = vertex3;
        verticies[3] = vertex4;

        countPolygonNormal();
    }

    public void countPolygonNormal() {
        polygonNormal = new SpaceObject3D();

        float[] nVector1 = new float[3];
        float[] nVector2 = new float[3];

        float vectorLength = (float)Math.sqrt(
                (verticies[1].x - verticies[0].x)*(verticies[1].x - verticies[0].x) +
                        (verticies[1].y - verticies[0].y)*(verticies[1].y - verticies[0].y) +
                        (verticies[1].z - verticies[0].z)*(verticies[1].z - verticies[0].z)
        );

        if (vectorLength != 0) {

            nVector1[0] = (verticies[1].x - verticies[0].x) / vectorLength;
            nVector1[1] = (verticies[1].y - verticies[0].y) / vectorLength;
            nVector1[2] = (verticies[1].z - verticies[0].z) / vectorLength;
        }
        else
            return;


        vectorLength = (float)Math.sqrt(
                (verticies[3].x - verticies[0].x)*(verticies[3].x - verticies[0].x) +
                        (verticies[3].y - verticies[0].y)*(verticies[3].y - verticies[0].y) +
                        (verticies[3].z - verticies[0].z)*(verticies[3].z - verticies[0].z)
        );

        if (vectorLength != 0) {
            nVector2[0] = (verticies[2].x - verticies[0].x) / vectorLength;
            nVector2[1] = (verticies[2].y - verticies[0].y) / vectorLength;
            nVector2[2] = (verticies[2].z - verticies[0].z) / vectorLength;
        }
        else
            return;

        polygonNormal.x = nVector2[1]*nVector1[2] - nVector2[2]*nVector1[1];
        polygonNormal.y = nVector2[2]*nVector1[0] - nVector2[0]*nVector1[2];
        polygonNormal.z = nVector2[0]*nVector1[1] - nVector2[1]*nVector1[0];

        polygonNormal.x = -polygonNormal.x;
        polygonNormal.y = -polygonNormal.y;
        polygonNormal.z = -polygonNormal.z;

        nVector1 = null;
        nVector2 = null;
    }

    public void renderPolygon(TextureCoords texture, GL2 gl, boolean isSelfNormal, boolean isTextureOn) {
        for (int i = 0; i < verticies.length; i++) {
            if (isSelfNormal) {
                gl.glNormal3f(polygonNormal.x, polygonNormal.y, polygonNormal.z);
                if (isTextureOn) {
                    if (i == 0)
                        gl.glTexCoord2f(texture.left(), texture.bottom());
                    if (i == 1)
                        gl.glTexCoord2f(texture.left(), texture.top());
                    if (i == 2)
                        gl.glTexCoord2f(texture.right(), texture.top());
                    if (i == 3)
                        gl.glTexCoord2f(texture.right(), texture.bottom());
                }
                else
                    gl.glColor3f(1.0f, 1.0f, 1.0f);

                gl.glVertex3f(verticies[i].x, verticies[i].y, verticies[i].z);
            } else {
                gl.glNormal3f(verticies[i].normal.x, verticies[i].normal.y, verticies[i].normal.z);
                if (isTextureOn) {
                    if (i == 0)
                        gl.glTexCoord2f(texture.left(), texture.bottom());
                    if (i == 1)
                        gl.glTexCoord2f(texture.left(), texture.top());
                    if (i == 2)
                        gl.glTexCoord2f(texture.right(), texture.top());
                    if (i == 3)
                        gl.glTexCoord2f(texture.right(), texture.bottom());
                }
                else
                    gl.glColor3f(1.0f, 1.0f, 1.0f);

                gl.glVertex3f(verticies[i].x, verticies[i].y, verticies[i].z);
            }
        }
    }

    public void invertNormals() {
        polygonNormal.x = -polygonNormal.x;
        polygonNormal.y = -polygonNormal.y;
        polygonNormal.z = -polygonNormal.z;
        for (int i = 0; i < verticies.length; i++) {
            verticies[i].normal.x = -verticies[i].normal.x;
            verticies[i].normal.y = -verticies[i].normal.y;
            verticies[i].normal.z = -verticies[i].normal.z;
        }
    }

    public void countFaceNormalFromVerticies() {
        polygonNormal = new SpaceObject3D();
        for (int i = 0; i < verticies.length; i++) {
            polygonNormal.x += verticies[i].normal.x;
            polygonNormal.y += verticies[i].normal.y;
            polygonNormal.z += verticies[i].normal.z;
        }

        float vectorLength = (float)Math.sqrt(
                polygonNormal.x * polygonNormal.x +
                        polygonNormal.y * polygonNormal.y +
                        polygonNormal.z * polygonNormal.z
        );

        polygonNormal.x /= vectorLength;
        polygonNormal.y /= vectorLength;
        polygonNormal.z /= vectorLength;
    }

    public boolean containsVerticies(Vertex3D v1, Vertex3D v2) {

        boolean isFoundOne = false;
        boolean isFoundTwo = false;

        for (int i = 0; i < verticies.length; i++) {
            if (verticies[i].x == v1.x && verticies[i].y == v1.y && verticies[i].z == v1.z ) {
                isFoundOne = true;
            }
            else if (verticies[i].x == v2.x && verticies[i].y == v2.y && verticies[i].z == v2.z ) {
                isFoundTwo = true;
            }

            if (isFoundOne && isFoundTwo)
                return true;
        }

        return false;
    }

    public boolean containsVertex(Vertex3D v) {
        for (int i = 0; i < verticies.length; i++) {
            if (verticies[i].x == v.x && verticies[i].y == v.y && verticies[i].z == v.z ) {
                return true;
            }
        }
        return false;
    }
}
