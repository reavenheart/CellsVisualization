package objects;

import com.jogamp.opengl.util.texture.TextureCoords;

import com.jogamp.opengl.GL2;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Denis on 26.04.2015.
 */
public class ObjModel {
    private ArrayList<Face3D> faces;
    private ArrayList<Polygon3D> polygons;
    private ArrayList<Face3D> interpolatedFaces;
    private ArrayList<Polygon3D> interpolatedPolygons;

    private String fileName = "/models/cells.obj";

    public boolean isSelfNormals;
    public boolean isTextureOn;
    public boolean isInterpolate;
    public int interpolateNumber;
    public boolean isRenderable;

    boolean isRed;
    boolean isGreen;
    boolean isBlue;

    public ObjModel() {
        faces = new ArrayList<>();
        polygons = new ArrayList<>();
        isTextureOn = true;
        isInterpolate = false;
        interpolateNumber = 2;
        readFile();

        isRenderable = true;

        //interpolate();
    }

    public ObjModel(boolean isRed, boolean isGreen, boolean isBlue) {
        faces = new ArrayList<>();
        polygons = new ArrayList<>();
        isTextureOn = true;
        isInterpolate = false;
        interpolateNumber = 2;
        isRenderable = true;

        this.isRed = isRed;
        this.isGreen = isGreen;
        this.isBlue = isBlue;

        if (isRed)
            fileName = "/models/red.obj";
        else if (isGreen)
            fileName = "/models/green.obj";
        else if (isBlue)
            fileName = "/models/blue.obj";

        readFile();

        //interpolate();
    }

    private void readFile() {

        fileName = ObjModel.class.getResource(fileName).getPath();

        ArrayList<Vertex3D> verticies = new ArrayList<>();
        ArrayList<SpaceObject3D> normals = new ArrayList<>();

        //File file = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(fileName));
            /*
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("v ")) {
                    line = line.substring(2);
                    String[] coords = line.split(" ");

                    Vertex3D vertex = new Vertex3D();
                    vertex.x = Float.parseFloat(coords[0]);
                    vertex.y = Float.parseFloat(coords[1]);
                    vertex.z = Float.parseFloat(coords[2]);

                    verticies.add(vertex);
                }
                else if (line.startsWith("vn ")) {
                    line = line.substring(3);
                    String[] coords = line.split(" ");

                    SpaceObject3D normal = new SpaceObject3D();
                    normal.x = Float.parseFloat(coords[0]);
                    normal.y = Float.parseFloat(coords[1]);
                    normal.z = Float.parseFloat(coords[2]);

                    normals.add(normal);
                }
                else if (line.startsWith("s ")) {
                    line = line.substring(2);

                    isSelfNormals = line.contentEquals("off");
                }
                else if (line.startsWith("f ")) {
                    line = line.substring(2);
                    String[] params = line.split(" ");

                    boolean isNormalFound = false;

                    ArrayList<Vertex3D> vs = new ArrayList<>();
                    SpaceObject3D fn = new SpaceObject3D();

                    for (int i = 0; i < params.length; i++) {
                        String faceParts[] = params[i].split("/");

                        if (faceParts.length > 1 && !isNormalFound) {
                            fn = normals.get(Integer.parseInt(faceParts[2]) - 1);
                            isNormalFound = true;
                        }
                        vs.add(verticies.get(Integer.parseInt(faceParts[0]) - 1));
                    }

                    Face3D face = new Face3D(vs, fn);

                    faces.add(face);
                }
            }

            for (Vertex3D vertex : verticies) {

                ArrayList<SpaceObject3D> faceNormals = new ArrayList<>();

                for (Face3D face1 : faces) {
                    for (Vertex3D vx : face1.verticies) {
                        if (vx.equals(vertex))
                            faceNormals.add(face1.faceNormal);
                    }
                }

                vertex.countNormal(faceNormals);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
            int numpolys = 0;
            float toppoint = 0;
            float bottompoint = 0;
            float leftpoint = 0;
            float rightpoint = 0;
            float farpoint = 0;
            float nearpoint = 0;

            int linecounter = 0;
            int facecounter = 0;
                boolean firstpass = true;
                String newline;
                while((newline = br.readLine()) != null){
                    linecounter++;
                    if(newline.length() > 0) {
                        newline = newline.trim();

                        //LOADS VERTEX COORDINATES
                        if (newline.startsWith("v ")) {
                            float coords[] = new float[4];
                            String coordstext[] = new String[4];
                            newline = newline.substring(2, newline.length());
                            StringTokenizer st = new StringTokenizer(newline, " ");
                            for (int i = 0; st.hasMoreTokens(); i++)
                                coords[i] = Float.parseFloat(st.nextToken());

                            if (firstpass) {
                                rightpoint = coords[0];
                                leftpoint = coords[0];
                                toppoint = coords[1];
                                bottompoint = coords[1];
                                nearpoint = coords[2];
                                farpoint = coords[2];
                                firstpass = false;
                            }
                            if (coords[0] > rightpoint)
                                rightpoint = coords[0];
                            if (coords[0] < leftpoint)
                                leftpoint = coords[0];
                            if (coords[1] > toppoint)
                                toppoint = coords[1];
                            if (coords[1] < bottompoint)
                                bottompoint = coords[1];
                            if (coords[2] > nearpoint)
                                nearpoint = coords[2];
                            if (coords[2] < farpoint)
                                farpoint = coords[2];
                            Vertex3D vertex = new Vertex3D();
                            vertex.x = coords[0];
                            vertex.y = coords[1];
                            vertex.z = coords[2];
                            verticies.add(vertex);
                        } else
                            //LOADS VERTEX NORMALS COORDINATES
                            if (newline.startsWith("vn")) {
                                float coords[] = new float[4];
                                String coordstext[] = new String[4];
                                newline = newline.substring(3, newline.length());
                                StringTokenizer st = new StringTokenizer(newline, " ");
                                for (int i = 0; st.hasMoreTokens(); i++)
                                    coords[i] = Float.parseFloat(st.nextToken());

                                SpaceObject3D normal = new SpaceObject3D();
                                normal.x = coords[0];
                                normal.y = coords[1];
                                normal.z = coords[2];

                                normals.add(normal);
                            } else

                                //LOADS FACES COORDINATES
                                if (newline.startsWith("f ")) {
                                        /*
                                        facecounter++;
                                        newline = newline.substring(2, newline.length());
                                        StringTokenizer st = new StringTokenizer(newline, " ");
                                        int count = st.countTokens();
                                        int v[] = new int[count];
                                        int vt[] = new int[count];
                                        int vn[] = new int[count];
                                        for(int i = 0; i < count; i++){
                                            char chars[] = st.nextToken().toCharArray();
                                            StringBuffer sb = new StringBuffer();
                                            char lc = 'x';
                                            for(int k = 0; k < chars.length; k++){
                                                if(chars[k] == '/' && lc == '/')
                                                    sb.append('0');
                                                lc = chars[k];
                                                sb.append(lc);
                                            }

                                            StringTokenizer st2 = new StringTokenizer
                                                    (sb.toString(), "/");
                                            int num = st2.countTokens();
                                            v[i] = Integer.parseInt(st2.nextToken());
                                            if(num > 1)
                                                vt[i] = Integer.parseInt(st2.nextToken());
                                            else
                                                vt[i] = 0;
                                            if(num > 2)
                                                vn[i] = Integer.parseInt(st2.nextToken());
                                            else
                                                vn[i] = 0;
                                        }

                                        faces.add(v);
                                        facestexs.add(vt);
                                        facesnorms.add(vn);
                                        */
                                    newline = newline.substring(2);
                                    String[] params = newline.split(" ");

                                    boolean isNormalFound = false;

                                    ArrayList<Vertex3D> vs = new ArrayList<>();
                                    SpaceObject3D fn = new SpaceObject3D();

                                    for (int i = 0; i < params.length; i++) {
                                        String faceParts[] = params[i].split("/");

                                        if (faceParts.length > 1 && !isNormalFound) {
                                            fn = normals.get(Integer.parseInt(faceParts[2]) - 1);
                                            isNormalFound = true;
                                        }
                                        vs.add(verticies.get(Integer.parseInt(faceParts[0]) - 1));
                                    }

                                    Face3D face = new Face3D(vs, fn);

                                    faces.add(face);
                                }
                    }
                }
            }
            catch(IOException e){
                System.out.println("Failed to read file: " + br.toString());
            }
            catch(NumberFormatException e){
                System.out.println("Malformed OBJ file: " + br.toString() + "\r \r"+ e.getMessage());
            }

        //interpolate();
    }

    public void invertNormals() {

        for (int i = 0; i < polygons.size(); i++) {
            polygons.get(i).invertNormals();
        }
    }

    public void render(GL2 gl, TextureCoords textureCoords) {
        if (isRenderable) {

            if (!isTextureOn) {
                if (isRed)
                    gl.glColor3f(1.0f, 0f, 0f);
                else if (isGreen)
                    gl.glColor3f(0.0f, 1.0f, 0f);
                else if (isBlue)
                    gl.glColor3f(0f, 0f, 1.0f);
                else
                    gl.glColor3f(1.0f, 1.0f, 1.0f);
            }


            gl.glBegin(gl.GL_QUADS);

            if (isInterpolate) {
                for (Face3D face : interpolatedFaces) {
                    face.renderFace(textureCoords, gl, isSelfNormals, isTextureOn);
                }
            } else {
                for (Face3D face : faces) {
                    face.renderFace(textureCoords, gl, isSelfNormals, isTextureOn);
                }
            }

            gl.glEnd();
        }
    }

    public void interpolate() {

        interpolatedFaces = new ArrayList<>();

        for (Face3D face : faces) {
            ArrayList<Vertex3D> newVerticies = new ArrayList<>();
            ArrayList<Face3D> adjacentFaces = new ArrayList<>();

            Vertex3D F = new Vertex3D();
            Vertex3D R;

            F.x = face.faceCenter.x;
            F.y = face.faceCenter.y;
            F.z = face.faceCenter.z;

            F.normal.x = face.faceNormal.x;
            F.normal.y = face.faceNormal.y;
            F.normal.z = face.faceNormal.z;

            newVerticies.add(F);

            for (int i = 0; i < face.verticies.size(); i++) {

                adjacentFaces = new ArrayList<>();

                for (Face3D f : faces) {
                    if (f.containsVertex(face.verticies.get(i))) {
                        adjacentFaces.add(f);
                    }
                }

                F = new Vertex3D();
                R = new Vertex3D();

                ArrayList<SpaceObject3D> edgeCenters = new ArrayList<>();
                ArrayList<Edge3D> edges = new ArrayList<>();

                for (Face3D f : adjacentFaces) {
                    F.x += f.faceCenter.x;
                    F.y += f.faceCenter.y;
                    F.z += f.faceCenter.z;

                    for (Edge3D e : f.edges) {
                        if (e.containsVertex(face.verticies.get(i))) {
                            edgeCenters.add(e.edgeCenter);
                            edges.add(e);
                        }
                    }
                }

                for (int l = 0; l < edgeCenters.size(); l++) {
                    for (int k = edgeCenters.size() - 1; k > l; k--) {
                        if (edgeCenters.get(l).positionEquals(edgeCenters.get(k))) {
                            edgeCenters.remove(k);
                        }
                    }
                }

                F.x /= adjacentFaces.size();
                F.y /= adjacentFaces.size();
                F.z /= adjacentFaces.size();

                for (SpaceObject3D so : edgeCenters) {
                    R.x += so.x;
                    R.y += so.y;
                    R.z += so.z;
                }

                R.x /= edgeCenters.size();
                R.y /= edgeCenters.size();
                R.z /= edgeCenters.size();

                //middleVertex.x = (middleVertex.x + middleVertex.x + face.verticies.get(i).x) / 3;
                //middleVertex.y = (middleVertex.y + middleVertex.y + face.verticies.get(i).y) / 3;
                //middleVertex.z = (middleVertex.z + middleVertex.z + face.verticies.get(i).z) / 3;

                F.x = (F.x + 2 * R.x + (adjacentFaces.size() - 3) * face.verticies.get(i).x) / adjacentFaces.size();
                F.y = (F.y + 2 * R.y + (adjacentFaces.size() - 3) * face.verticies.get(i).y) / adjacentFaces.size();
                F.z = (F.z + 2 * R.z + (adjacentFaces.size() - 3) * face.verticies.get(i).z) / adjacentFaces.size();

                SpaceObject3D normal = new SpaceObject3D();

                for (Face3D f : adjacentFaces) {
                    normal.x += f.faceNormal.x;
                    normal.y += f.faceNormal.y;
                    normal.z += f.faceNormal.z;
                }

                normal.x /= adjacentFaces.size();
                normal.y /= adjacentFaces.size();
                normal.z /= adjacentFaces.size();

                F.normal.x = normal.x;
                F.normal.y = normal.y;
                F.normal.z = normal.z;

                newVerticies.add(F);

                Edge3D edge;
                if (i < face.verticies.size() - 1)
                    edge = face.getEdge(face.verticies.get(i), face.verticies.get(i+1));
                else
                    edge = face.getEdge(face.verticies.get(i), face.verticies.get(0));

                if (edge != null) {
                    for (int j = 0; j < adjacentFaces.size(); j++) {
                        if (!adjacentFaces.get(j).containsEdge(edge)) {
                            adjacentFaces.remove(j);
                            j--;
                        }
                    }

                    F = new Vertex3D();

                    for (Face3D f : adjacentFaces) {
                        F.x += f.faceCenter.x;
                        F.y += f.faceCenter.y;
                        F.z += f.faceCenter.z;
                    }

                    F.x /= adjacentFaces.size();
                    F.y /= adjacentFaces.size();
                    F.z /= adjacentFaces.size();

                    F.x = (F.x + edge.edgeCenter.x) / 2;
                    F.y = (F.y + edge.edgeCenter.y) / 2;
                    F.z = (F.z + edge.edgeCenter.z) / 2;

                    normal = new SpaceObject3D();

                    for (Face3D f : adjacentFaces) {
                        normal.x += f.faceNormal.x;
                        normal.y += f.faceNormal.y;
                        normal.z += f.faceNormal.z;
                    }

                    normal.x /= adjacentFaces.size();
                    normal.y /= adjacentFaces.size();
                    normal.z /= adjacentFaces.size();

                    F.normal.x = normal.x;
                    F.normal.y = normal.y;
                    F.normal.z = normal.z;

                    newVerticies.add(F);
                }

            }


            Face3D face3D = new Face3D(newVerticies.get(1), newVerticies.get(2), newVerticies.get(0), newVerticies.get(8));
            interpolatedFaces.add(face3D);

            face3D = new Face3D(newVerticies.get(0), newVerticies.get(2), newVerticies.get(3), newVerticies.get(4));
            interpolatedFaces.add(face3D);

            face3D = new Face3D(newVerticies.get(0), newVerticies.get(4), newVerticies.get(5), newVerticies.get(6));
            interpolatedFaces.add(face3D);

            face3D = new Face3D(newVerticies.get(0), newVerticies.get(6), newVerticies.get(7), newVerticies.get(8));
            interpolatedFaces.add(face3D);
        }
    }
}
