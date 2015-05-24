package objects;

import com.jogamp.opengl.util.texture.TextureCoords;

import javax.media.opengl.GL2;
import java.util.ArrayList;

/**
 * Created by Denis on 26.04.2015.
 */
public class Face3D {
    public ArrayList<Vertex3D> verticies;
    public ArrayList<Edge3D> edges;

    public SpaceObject3D faceNormal;
    public SpaceObject3D faceCenter;

    public Face3D() {
        verticies = new ArrayList<>();
        edges = new ArrayList<>();

        faceNormal = new SpaceObject3D();
        faceCenter = new SpaceObject3D();
    }

    public Face3D(Vertex3D vertex1, Vertex3D vertex2, Vertex3D vertex3) {
        verticies = new ArrayList<>();
        edges = new ArrayList<>();

        verticies.add(vertex1);
        verticies.add(vertex2);
        verticies.add(vertex3);

        for (int i = 0; i < verticies.size(); i++) {
            if (i < verticies.size() - 1) {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(i + 1));
                edges.add(edge);
            }
            else {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(0));
                edges.add(edge);
            }
        }

        countFaceNormal();
        countFaceCenter();
    }

    public Face3D(Vertex3D vertex1, Vertex3D vertex2, Vertex3D vertex3, Vertex3D vertex4) {
        verticies = new ArrayList<>();
        edges = new ArrayList<>();

        verticies.add(vertex1);
        verticies.add(vertex2);
        verticies.add(vertex3);
        verticies.add(vertex4);

        for (int i = 0; i < verticies.size(); i++) {
            if (i < verticies.size() - 1) {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(i + 1));
                edges.add(edge);
            }
            else {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(0));
                edges.add(edge);
            }
        }

        countFaceNormal();
        countFaceCenter();
    }

    public Face3D(ArrayList<Vertex3D> verticies) {
        this.verticies = verticies;
        edges = new ArrayList<>();

        for (int i = 0; i < verticies.size(); i++) {
            if (i < verticies.size() - 1) {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(i + 1));
                edges.add(edge);
            }
            else {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(0));
                edges.add(edge);
            }
        }

        countFaceNormal();
        countFaceCenter();
    }

    public Face3D(ArrayList<Vertex3D> verticies, SpaceObject3D faceNormal) {
        this.verticies = verticies;
        edges = new ArrayList<>();

        for (int i = 0; i < verticies.size(); i++) {
            if (i < verticies.size() - 1) {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(i + 1));
                edges.add(edge);
            }
            else {
                Edge3D edge = new Edge3D(verticies.get(i), verticies.get(0));
                edges.add(edge);
            }
        }

        this.faceNormal = faceNormal;
        countFaceCenter();
    }

    private void countFaceCenter() {

        faceCenter = new SpaceObject3D();

        for (Vertex3D vertex : verticies) {
            faceCenter.x += vertex.x;
            faceCenter.y += vertex.y;
            faceCenter.z += vertex.z;
        }

        faceCenter.x /= verticies.size();
        faceCenter.y /= verticies.size();
        faceCenter.z /= verticies.size();
    }

    public void countFaceNormal() {
        faceNormal = new SpaceObject3D();

        float[] nVector1 = new float[3];
        float[] nVector2 = new float[3];

        float vectorLength = (float)Math.sqrt(
                (verticies.get(1).x - verticies.get(0).x)*(verticies.get(1).x - verticies.get(0).x) +
                (verticies.get(1).y - verticies.get(0).y)*(verticies.get(1).y - verticies.get(0).y) +
                (verticies.get(1).z - verticies.get(0).z)*(verticies.get(1).z - verticies.get(0).z)
        );

        if (vectorLength != 0) {

            nVector1[0] = (verticies.get(1).x - verticies.get(0).x) / vectorLength;
            nVector1[1] = (verticies.get(1).y - verticies.get(0).y) / vectorLength;
            nVector1[2] = (verticies.get(1).z - verticies.get(0).z) / vectorLength;
        }
        else
            return;


        vectorLength = (float)Math.sqrt(
                (verticies.get(3).x - verticies.get(0).x)*(verticies.get(3).x - verticies.get(0).x) +
                (verticies.get(3).y - verticies.get(0).y)*(verticies.get(3).y - verticies.get(0).y) +
                (verticies.get(3).z - verticies.get(0).z)*(verticies.get(3).z - verticies.get(0).z)
        );

        if (vectorLength != 0) {
            nVector2[0] = (verticies.get(3).x - verticies.get(0).x) / vectorLength;
            nVector2[1] = (verticies.get(3).y - verticies.get(0).y) / vectorLength;
            nVector2[2] = (verticies.get(3).z - verticies.get(0).z) / vectorLength;
        }
        else
            return;

        faceNormal.x = nVector2[1]*nVector1[2] - nVector2[2]*nVector1[1];
        faceNormal.y = nVector2[2]*nVector1[0] - nVector2[0]*nVector1[2];
        faceNormal.z = nVector2[0]*nVector1[1] - nVector2[1]*nVector1[0];

        faceNormal.x = -faceNormal.x;
        faceNormal.y = -faceNormal.y;
        faceNormal.z = -faceNormal.z;
    }

    public void renderFace(TextureCoords texture, GL2 gl, boolean isSelfNormal, boolean isTextureOn) {
        for (int i = 0; i < verticies.size(); i++) {
            if (isSelfNormal) {
                gl.glNormal3f(faceNormal.x, faceNormal.y, faceNormal.z);
                if (isTextureOn) {
                    if (i == 0)
                        gl.glTexCoord2f(texture.left(), texture.bottom());
                    else if (i == 1)
                        gl.glTexCoord2f(texture.left(), texture.top());
                    else if (i == 2)
                        gl.glTexCoord2f(texture.right(), texture.top());
                    else if (i == 3)
                        gl.glTexCoord2f(texture.right(), texture.bottom());
                }
                else
                    gl.glColor3f(1.0f, 1.0f, 1.0f);

                gl.glVertex3f(verticies.get(i).x, verticies.get(i).y, verticies.get(i).z);
            } else {
                gl.glNormal3f(verticies.get(i).normal.x, verticies.get(i).normal.y, verticies.get(i).normal.z);
                if (isTextureOn) {
                    if (i == 0)
                        gl.glTexCoord2f(texture.left(), texture.bottom());
                    else if (i == 1)
                        gl.glTexCoord2f(texture.left(), texture.top());
                    else if (i == 2)
                        gl.glTexCoord2f(texture.right(), texture.top());
                    else if (i == 3)
                        gl.glTexCoord2f(texture.right(), texture.bottom());
                }
                else
                    gl.glColor3f(1.0f, 1.0f, 1.0f);

                gl.glNormal3f(verticies.get(i).normal.x, verticies.get(i).normal.y, verticies.get(i).normal.z);
                gl.glVertex3f(verticies.get(i).x, verticies.get(i).y, verticies.get(i).z);
            }
        }
    }

    public void countFaceNormalFromVerticies() {
        faceNormal = new SpaceObject3D();
        for (int i = 0; i < verticies.size(); i++) {
            faceNormal.x += verticies.get(i).normal.x;
            faceNormal.y += verticies.get(i).normal.y;
            faceNormal.z += verticies.get(i).normal.z;
        }

        float vectorLength = (float)Math.sqrt(
                faceNormal.x * faceNormal.x +
                        faceNormal.y * faceNormal.y +
                        faceNormal.z * faceNormal.z
        );

        faceNormal.x /= vectorLength;
        faceNormal.y /= vectorLength;
        faceNormal.z /= vectorLength;
    }

    public boolean containsVertex(Vertex3D vertex) {

        for (Vertex3D v : verticies)
            if (v.equals(vertex))
                return true;

        return false;
    }

    public Edge3D getEdge(Vertex3D v1, Vertex3D v2) {

        for (Edge3D edge : edges) {
            if (edge.containsVerticies(v1, v2))
                return edge;
        }

        return null;
    }

    public boolean containsEdge(Edge3D edge) {
        for (Edge3D e : edges) {
            if (e.containsVerticies(edge.v1, edge.v2))
                return true;
        }

        return false;
    }
}
