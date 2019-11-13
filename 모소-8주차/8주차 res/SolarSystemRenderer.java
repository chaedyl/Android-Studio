package edu.inha.solarsystem_obj_demo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.inha.solarsystem_obj_demo.Parser.ObjParser;
import edu.inha.solarsystem_obj_demo.Parser.ObjStructure;

public class SolarSystemRenderer implements GLSurfaceView.Renderer {
    private Context context;

    /* For obj(planet) & texture */
    private ObjStructure[] planet = new ObjStructure[10];

    public int[] texture_id = new int[]{    // 태양, 수성, 금성, 지구, 달, 화성, 목성, 토성, 천왕성, 해왕성
            R.drawable.sun,R.drawable.mercury,R.drawable.venus,
            R.drawable.earth,R.drawable.moon,R.drawable.mars,
            R.drawable.jupiter, R.drawable.saturn, R.drawable.uranus,
            R.drawable.neptune};
    public float scaler = .5f;  // 태양 크기 결정
    public float[] scalefactor = new float[]{   // 태양으로부터 상대적 크기 결정
            scaler, scaler*0.1f, scaler*0.2f,   // 태양, 수성, 금성
            scaler*0.25f, scaler*0.08f, scaler*0.18f,    // 지구, 달, 화성
            scaler*0.5f, scaler*0.4f,scaler*0.3f,scaler*0.3f};  // 목성, 토성, 천왕성, 해왕성

    /* For rotation */
    public boolean rot_flag = true;
    private float rot_sun = 360.0f;
    private float angle_earth = 0.0f;
    private float orbital_earth = 1.0f;

    /* For camera setting */
    private double distance;
    public volatile double elev;
    public volatile double azim;

    private float[] cam_eye = new float[3];
    private float[] cam_center = new float[3];
    private float[] cam_up = new float[3];
    private float[] cam_vpn = new float[3];
    private float[] cam_x_axis = new float[3];

    private float[] uv_py = new float[3];
    private float[] uv_ny = new float[3];

    /* For texture on, off */
    public boolean texture_on_off = false;

    public SolarSystemRenderer(Context context) {
        this.context = context;
    }

    private void calcCross(float[] vector1, float[] vector2, float[] cp_vector) {
        cp_vector[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        cp_vector[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        cp_vector[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];
    }

    private void vNorm(float[] vector) {
        float scale = (float) Math.sqrt(Math.pow((double) vector[0], 2) + Math.pow((double) vector[1], 2) + Math.pow((double) vector[2], 2));

        vector[0] = vector[0] / scale;
        vector[1] = vector[1] / scale;
        vector[2] = vector[2] / scale;
    }

    private void calcUpVector() {
        double r_elev = elev * Math.PI / 180.0;
        double r_azim = azim * Math.PI / 180.0;

        cam_eye[0] = (float) distance * (float) Math.sin(r_elev) * (float) Math.sin(r_azim);
        cam_eye[1] = (float) distance * (float) Math.cos(r_elev);
        cam_eye[2] = (float) distance * (float) Math.sin(r_elev) * (float) Math.cos(r_azim);

        cam_vpn[0] = cam_eye[0] - cam_center[0];
        cam_vpn[1] = cam_eye[1] - cam_center[1];
        cam_vpn[2] = cam_eye[2] - cam_center[2];
        vNorm(cam_vpn);

        if (elev >= 0 && elev < 180) {
            calcCross(uv_py, cam_vpn, cam_x_axis);
        }
        else {
            calcCross(uv_ny, cam_vpn, cam_x_axis);

        }
        calcCross(cam_vpn, cam_x_axis, cam_up);
        vNorm(cam_up);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glHint(gl.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_FASTEST);
        gl.glEnable(gl.GL_DEPTH_TEST);
        gl.glEnable(gl.GL_CULL_FACE);
        gl.glCullFace(gl.GL_BACK);

        distance = 10.0;
        elev = 90.0;
        azim = 0.0;

        uv_py[0] = 0.0f;
        uv_py[1] = 1.0f;
        uv_py[2] = 0.0f;

        uv_ny[0] = 0.0f;
        uv_ny[1] = -1.0f;
        uv_ny[2] = 0.0f;

        cam_center[0] = 0.0f;
        cam_center[1] = 0.0f;
        cam_center[2] = 0.0f;

        calcUpVector();

        for(int i=0; i<4;i ++){
            ObjParser objParser = new ObjParser(context); // obj 파일 Parser 생성
            try {
                objParser.parse(R.raw.planet); // obj 파일 parsing

            } catch (IOException e) {

            }
            int group = objParser.getObjectIds().size(); // 몇 개의 obj 파일이 있는지 확인
            int[] texture = new int[group];
            texture[0] = texture_id[i]; // texture 파일 설정

            planet[i] = new ObjStructure(objParser, gl, this.context, texture); // objstructure 생성
        }
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float zNear = 0.1f;
        float zFar = 1000f;
        float fovy = 45.0f;
        float aspect = (float) width / (float) height;

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU.gluPerspective(gl, fovy, aspect, zNear, zFar);
        gl.glViewport(0, 0, width, height);
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        calcUpVector();

        GLU.gluLookAt(gl, cam_eye[0], cam_eye[1], cam_eye[2], cam_center[0], cam_center[1], cam_center[2], cam_up[0], cam_up[1], cam_up[2]);

        if(texture_on_off){
            gl.glEnable(GL10.GL_TEXTURE_2D);
        }else{
            gl.glDisable(GL10.GL_TEXTURE_2D);
        }
        gl.glPushMatrix();
            gl.glRotatef(rot_sun, 0.0f, 1.0f, 0.0f); // 태양의 자전
            // draw Sun
            gl.glColor4f(1.0f,0.0f,0.0f,1.0f);
            planet[0].setScale(scalefactor[0]);
            planet[0].draw(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
            gl.glRotatef(angle_earth, 0.0f, 1.0f, 0.0f);
            gl.glTranslatef(2.0f, 0.0f, 0.0f);

            // draw Earth
            gl.glColor4f(0.0f,0.0f,1.0f,1.0f);
            planet[3].setScale(scalefactor[3]);
            planet[3].draw(gl);
        gl.glPopMatrix();

        if(rot_flag) {
            rot_sun -= 0.2f;
            angle_earth += orbital_earth;

            if (angle_earth >= 360.0f) {
                angle_earth -= 360.0f;
            }
        }

        gl.glFlush();
    }
}
