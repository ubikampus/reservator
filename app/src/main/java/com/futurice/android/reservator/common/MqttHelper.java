package com.futurice.android.reservator.common;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fi.helsinki.ubimqtt.IUbiActionListener;
import fi.helsinki.ubimqtt.IUbiMessageListener;
import fi.helsinki.ubimqtt.UbiMqtt;

public class MqttHelper {
    private static MqttHelper instance;

    public static MqttHelper getInstance(Context context) {
        if (instance == null)
            instance = new MqttHelper(context);
        return instance;
    }

    public static MqttHelper getInstance(Context context, boolean recreate) {
        if (recreate == false && instance != null)
            instance.disconnect();

        return getInstance(context);
    }

    private static final String MQTT_SERVER_ADDRESS = "localhost";


    private Context context;
    private String privateKey = null;
    private String airQualityPublicKey = null;
    private UbiMqtt ubiMqtt = null;

    private MqttHelper(Context context) {
        this.context = context;
    }


    private AirQualityListener airQualityListener = null;

    public void setAirQualityListener(AirQualityListener lis) {
        this.airQualityListener = lis;
    }

    private static String readStringFromFile(Context context, String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        //try (BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(filePath)))) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return contentBuilder.toString();
    }

    private static JSONObject createStatusObject(String currentId, String currentTopic, long currentStartTime, long currentEndTime,
                                                 String nextId, String nextTopic, long nextStartTime, long nextEndTime) throws JSONException {
        JSONObject ret = new JSONObject();

        if (currentTopic != null)
            ret.put("currentId", currentId);

        if (currentTopic != null)
            ret.put("currentTopic", currentTopic);

        if (currentStartTime > 0)
            ret.put("currentStartTime", currentStartTime);

        if (currentEndTime > 0)
            ret.put("currentEndTime", currentEndTime);

        if (nextId != null)
            ret.put("nextId", nextId);

        if (nextTopic != null)
            ret.put("nextTopic", nextTopic);

        if (nextStartTime > 0)
            ret.put("nextStartTime", nextStartTime);

        if (nextEndTime > 0)
            ret.put("nextEndTime", nextEndTime);

        return ret;
    }

    private void subscribeToAirQuality() {

        String mqttAirQualityTopic = PreferenceManager.getInstance(context).getMqttAirQualityTopic();

        Log.d("reservator", "mqttAirQualityTopic " + mqttAirQualityTopic);
        if (mqttAirQualityTopic == null)
            return;

        /* It seems that the java version of ubimqtt is not able to check the signatures made with javascript version
        this.airQualityPublicKey = readStringFromFile(context, "/sdcard/thunderboard-public-key.pem");

        if (this.airQualityPublicKey != null) {
            String[] keys = new String[1];
            keys[0] = this.airQualityPublicKey;
        */
            ubiMqtt.subscribe(mqttAirQualityTopic, new IUbiMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage, String listenerId) throws Exception {
                    Log.d("reservator", "messageArrived() " + mqttMessage.toString());
                    try {
                        JSONParser parser = new JSONParser();
                        JSONObject obj = (JSONObject) parser.parse(mqttMessage.toString());

                        String payload = (String) obj.get("payload");


                        JSONObject payloadObj = (JSONObject) parser.parse(payload);

                        JSONObject readings = (JSONObject) payloadObj.get("readings");


                        Long co2 = (Long) readings.get("co2");
                        Long voc = (Long) readings.get("voc");

                        if (airQualityListener != null) {
                            airQualityListener.onAirQualityReading(co2.intValue(), voc.intValue());
                        }
                    } catch (Exception e) {
                        Log.d("reservator", "JSON parse error " + e.toString());
                    }
                }
            }, new IUbiActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        /*
        }
        else {
            Log.d("reservator", "airQualityPublicKey was null");
        }

         */
    }

    private void connect(final IUbiActionListener listener) {
        if (ubiMqtt != null) {
            listener.onSuccess(null);
            return;
        }
        String serverAddress = PreferenceManager.getInstance(context).getMqttServerAddress();
        Log.d("MqttHelper", "serverAddress: " + serverAddress);
        if (serverAddress != null) {
            privateKey = readStringFromFile(context, "/sdcard/tablets-private-key.pem");
            if (privateKey != null) {
                ubiMqtt = null;
                final UbiMqtt tempUbiMqtt = new UbiMqtt(serverAddress);
                tempUbiMqtt.connect(new IUbiActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("MqttHelper", "Connecting to mqtt server succeeded");
                        ubiMqtt = tempUbiMqtt;

                        subscribeToAirQuality();

                        listener.onSuccess(asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("Mqtthelper", "Connecting to mqtt server failed");
                        listener.onFailure(asyncActionToken, exception);
                    }
                });
            }
        }
    }

    public void disconnect() {
        if (ubiMqtt != null) {
            ubiMqtt.disconnect(new IUbiActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MqttHelper", "MqttHelper::disconnect() disconnection succeeded");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("MqttHelper", "MqttHelper::disconnect() disconnection failed");
                }
            });
        }
        ubiMqtt = null;
    }

    public void reportReservationStarting(final String currentId, final String currentTopic, final long currentStartTime, final long currentEndTime,
                                          final String nextId, final String nextTopic, final long nextStartTime, final long nextEndTime) {
        this.connect(new IUbiActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    String topic = PreferenceManager.getInstance(context).getMqttPrefix() + "/starting";

                    JSONObject statusObject = createStatusObject(currentId, currentTopic, currentStartTime, currentEndTime,
                            nextId, nextTopic, nextStartTime, nextEndTime);

                    String message = statusObject.toString();

                    ubiMqtt.publishSigned(topic, message, privateKey, new IUbiActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationStarting() Mqtt publish succeeded");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationStarting() Mqtt publish failed");
                            exception.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MqttHelper", "MqttHelper::reportReservationStarting() Connecting to Mqtt failed");
            }
        });
    }

    public void reportReservationEnding(final String currentId, final String currentTopic, final long currentStartTime, final long currentEndTime,
                                        final String nextId, final String nextTopic, final long nextStartTime, final long nextEndTime) {
        this.connect(new IUbiActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    String topic = PreferenceManager.getInstance(context).getMqttPrefix() + "/ending";

                    JSONObject statusObject = createStatusObject(currentId, currentTopic, currentStartTime, currentEndTime,
                            nextId, nextTopic, nextStartTime, nextEndTime);

                    String message = statusObject.toString();

                    ubiMqtt.publishSigned(topic, message, privateKey, new IUbiActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationEnding() Mqtt publish succeeded");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationEnding() Mqtt publish failed");
                            exception.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MqttHelper", "MqttHelper::reportReservationEnding() Connecting to Mqtt failed");
            }
        });
    }

    public void reportReservationStatus(final String currentId, final String currentTopic, final long currentStartTime, final long currentEndTime,
                                        final String nextId, final String nextTopic, final long nextStartTime, final long nextEndTime) {
        this.connect(new IUbiActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    String topic = PreferenceManager.getInstance(context).getMqttPrefix() + "/status";

                    JSONObject statusObject = createStatusObject(currentId, currentTopic, currentStartTime, currentEndTime,
                            nextId, nextTopic, nextStartTime, nextEndTime);

                    String message = statusObject.toString();

                    ubiMqtt.publishSigned(topic, message, privateKey, new IUbiActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationStatus() Mqtt publish succeeded");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d("MqttHelper", "MqttHelper::reportReservationStatus() Mqtt publish failed");
                            exception.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d("MqttHelper", "MqttHelper::reportReservationStatus() Connecting to Mqtt failed");
            }
        });
    }
}
