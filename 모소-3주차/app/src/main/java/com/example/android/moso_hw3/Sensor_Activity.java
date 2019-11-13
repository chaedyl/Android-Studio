package com.example.android.moso_hw3;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Sensor_Activity extends AppCompatActivity implements SensorEventListener {

    SensorManager mSensorManager; //센서를 가져올 센서 매니져 변수 선언
    Sensor mSensor; //각 센서들에 대한 변수 선언 (ACCEL sensor)
    Sensor gSensor; // (GYRO senseor)

    ListView list;
    private ArrayList mySensorList;

    TextView data; // accelerometer 의 data 를 받아올 textview
    TextView data2; // 추가 센서의 data 를 받아올 textview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_);


        list = (ListView) findViewById(R.id.sensorlist);
        data = (TextView) findViewById(R.id.text_data);
        data2 = (TextView) findViewById(R.id.text_data2);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // mSensorManager 이용해서 sensor service 의 reference 받음 , sensor 장치 목록을 확인한다.

        mySensorList = new ArrayList();
        final List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL); // 센서 리스트를 담을 deviceSensors


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){ // accelerometer 존재하면
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);  // accelerometer 센서와 연결
        }

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        for (Sensor s : deviceSensors) {
            mySensorList.add(s.getName());
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mySensorList);
        list.setAdapter(adapter); // 센서 객체들을 리스트뷰에 표시하기 위한 어댑터 객체 생성
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, gSensor,mSensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        if(sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            float[] v = sensorEvent.values;
            data.setText("Accelerometer" + '\n' + "x = " + v[0] + '\n' + "y = " + v[1] + '\n' + "z = " + v[2] + '\n');
        }

        if(sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            float[] v1 = sensorEvent.values;
            data2.setText("GYROSCOPE" + '\n' + "x = " + v1[0] + '\n' + "y = " + v1[1] + '\n' + "z = " + v1[2] + '\n');
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
