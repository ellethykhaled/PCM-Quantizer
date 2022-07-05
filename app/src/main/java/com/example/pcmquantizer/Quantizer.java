package com.example.pcmquantizer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;


public class Quantizer extends AppCompatActivity {

    EditText etTimeVector;
    EditText etAmplitudeVector;

    LinkedList<Double> timeVector;
    LinkedList<Double> amplitudeVector;
    LinkedList<Double> quantizedAmplitudeVector;


    Button UQuantizer;
    Button NUQuantizer;

    EditText etLevels;
    EditText etPeak;

    int Levels;
    double Peak;

    ConstraintLayout uniformOption;
    LinearLayout nonuniformOption;

    EditText etMu;

    int Mu;

    ImageView midRise;
    ImageView midTread;

    boolean uniformQ = true;
    boolean midRiseB = true;

    GraphView graph;

    LinkedList<LineGraphSeries<DataPoint>> continuousSignal;
    LinkedList<LineGraphSeries<DataPoint>> quantizedSignal;

    TextView EMSValue;

    Button Sample;

    String xTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_quantizer);

        if (Build.VERSION.SDK_INT >= 16) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        etTimeVector = findViewById(R.id.etTimeVector);
        etAmplitudeVector = findViewById(R.id.etAmplitudeVector);

        etLevels = findViewById(R.id.etLevels);
        etPeak = findViewById(R.id.etPeak);

        etMu = findViewById(R.id.etMu);

        etTimeVector.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_PHONE);
        etAmplitudeVector.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_PHONE);

        etLevels.setInputType(InputType.TYPE_CLASS_NUMBER);
        etPeak.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etMu.setInputType(InputType.TYPE_CLASS_NUMBER);

        UQuantizer = findViewById(R.id.UQ);
        NUQuantizer = findViewById(R.id.NUQ);

        uniformOption = findViewById(R.id.UniformOptions);
        nonuniformOption = findViewById(R.id.NonUniformOptions);

        midRise = findViewById(R.id.midRise);
        midTread = findViewById(R.id.midTread);

        graph = findViewById(R.id.graph);

        EMSValue = findViewById(R.id.tvEMSValue);

        Sample = findViewById(R.id.Sample);

        timeVector = new LinkedList<>();
        amplitudeVector = new LinkedList<>();
        quantizedAmplitudeVector = new LinkedList<>();

        UQuantizer.setOnClickListener(view -> {
            if (!uniformQ) {
                UQuantizer.setTextColor(Color.RED);
                NUQuantizer.setTextColor(Color.WHITE);
                uniformOption.setVisibility(View.VISIBLE);
                nonuniformOption.setVisibility(View.GONE);
                uniformQ = true;
            }
        });
        NUQuantizer.setOnClickListener(view -> {
            if (uniformQ) {
                NUQuantizer.setTextColor(Color.RED);
                UQuantizer.setTextColor(Color.WHITE);
                nonuniformOption.setVisibility(View.VISIBLE);
                uniformOption.setVisibility(View.GONE);
                uniformQ = false;
            }
        });

        midRise.setOnClickListener(view -> {
            if (!midRiseB) {
                midTread.setImageResource(R.drawable.untick);
                midRise.setImageResource(R.drawable.tick);
                midRiseB = true;
            }
        });
        midTread.setOnClickListener(view -> {
            if (midRiseB) {
                midRise.setImageResource(R.drawable.untick);
                midTread.setImageResource(R.drawable.tick);
                midRiseB = false;
            }
        });

        Sample.setOnClickListener(view -> sample());
    }

    private void sample() {
        if (!validVector(etTimeVector.getText().toString()) || etTimeVector.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter time vector as numbers\nseparated by Spaces!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!validVector(etAmplitudeVector.getText().toString()) || etAmplitudeVector.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter amplitude vector as numbers\nseparated by Spaces!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!validVector(etLevels.getText().toString()) || etLevels.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter a valid number of Levels!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!validVector(etPeak.getText().toString()) || etPeak.getText().toString().length() < 1) {
            Toast.makeText(getBaseContext(), "Enter a valid Peak Value!", Toast.LENGTH_SHORT).show();
            return;
        } else if (!uniformQ) {
            if (!validVector(etMu.getText().toString()) || etMu.getText().toString().length() < 1) {
                char c = 181;
                String str = "Enter a valid value for " + c + "!";
                Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Log.d("Sample", "Valid");
        if (createVectors()) {
            Log.d("Sample", "Vector Created");
            drawContinuousAndQuantized();
        }

    }

    private boolean validVector(String str) {
        try {
            boolean number = false;
            for (int i = 0; i < str.length(); i++) {
                boolean b = str.charAt(i) != ' ' && str.charAt(i) != '.' && str.charAt(i) != '-';
                if ((str.charAt(i) < '0' || str.charAt(i) > '9') && b)
                    return false;
                else if (b)
                    number = true;
            }
            return number;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean createVectors() {
        timeVector.clear();
        amplitudeVector.clear();
        quantizedAmplitudeVector.clear();
        String time = etTimeVector.getText().toString().trim();
        int j = 0;
        for (int i = 0; i < time.length(); i++) {
            if (time.charAt(i) == ' ') {
                if (time.substring(j, i).equals("."))
                    timeVector.add(Double.parseDouble("0"));
                else
                    timeVector.add(Double.parseDouble(time.substring(j, i)));
                j = i + 1;
            } else if (i == time.length() - 1) {
                if (time.substring(j, i + 1).equals("."))
                    timeVector.add(Double.parseDouble("0"));
                else
                    timeVector.add(Double.parseDouble(time.substring(j, i + 1)));
                j = i + 1;
            }
        }
        if (timeVector.get(0) != 0) {
            Toast.makeText(getBaseContext(), "Make sure time vector starts with time 0!", Toast.LENGTH_SHORT).show();
            return false;
        }
        double diff = timeVector.get(1) - timeVector.get(0);
        for (int i = 2; i < timeVector.size() - 1; i++) {
            if (Math.abs(timeVector.get(i + 1) - timeVector.get(i) - diff) > 0.000001) {
                Toast.makeText(getBaseContext(), "Make sure sampling frequency is constant!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        double factor = 1;
        int k = 0;
        while (timeVector.get(k) == 0)
            k++;
        while (timeVector.get(k) * factor < 1)
            factor *= 10;

        if (factor == 1)
            xTitle = "";
        else
            xTitle = String.valueOf(1 / factor);
        xTitle = xTitle.concat(" seconds");

        for (int z = 0; z < timeVector.size(); z++)
            timeVector.set(z, timeVector.get(z) * factor);

        time = etAmplitudeVector.getText().toString().trim();
        j = 0;
        for (int i = 0; i < time.length(); i++) {
            if (time.charAt(i) == ' ') {
                if (time.substring(j, i).equals("."))
                    amplitudeVector.add(Double.parseDouble("0"));
                else
                    amplitudeVector.add(Double.parseDouble(time.substring(j, i)));
                j = i + 1;
            } else if (i == time.length() - 1) {
                if (time.substring(j, i + 1).equals("."))
                    amplitudeVector.add(Double.parseDouble("0"));
                else
                    amplitudeVector.add(Double.parseDouble(time.substring(j, i + 1)));
                j = i + 1;
            }
        }

        if (timeVector.size() != amplitudeVector.size()) {
            Toast.makeText(getBaseContext(), "Make sure time vector has the same size as amplitude vector!", Toast.LENGTH_SHORT).show();
            return false;
        }

        Levels = Integer.parseInt(etLevels.getText().toString().trim());
        if (Levels < 2) {
            Toast.makeText(getBaseContext(), "Number of Levels must be greater than 1", Toast.LENGTH_SHORT).show();
            return false;
        }
        Peak = Double.parseDouble(etPeak.getText().toString().trim());
        if (Peak <= 0) {
            Toast.makeText(getBaseContext(), "Peak value must be greater than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!uniformQ) {
            Mu = Integer.parseInt(etMu.getText().toString().trim());
            double min = amplitudeVector.getFirst();
            double max = amplitudeVector.getFirst();
            for (int i = 1; i < amplitudeVector.size(); i++) {
                if (min > amplitudeVector.get(i))
                    min = amplitudeVector.get(i);
                if (max < amplitudeVector.get(i))
                    max = amplitudeVector.get(i);
            }
            for (int i = 0; i < amplitudeVector.size(); i++) {
                double y = (amplitudeVector.get(i) - min) / (max - min);
                y = (Math.log(1 + Mu * y)) / Math.log(1 + Mu);
                amplitudeVector.set(i, y);
                double step = 2.0 / Levels;
                double quantized = 0;
                if (Levels % 2 == 0)
                    quantized = step / 2;

                while (Math.abs(quantized - y) > step / 2)
                    quantized += step;
                quantizedAmplitudeVector.add(quantized);
            }
        } else {
            if (midRiseB) {
                double step = 2 * Peak / Levels;
                for (int i = 0; i < amplitudeVector.size(); i++) {
                    if (amplitudeVector.get(i) >= Peak - (step / 2))
                        quantizedAmplitudeVector.add(Peak - (step / 2));
                    else if (amplitudeVector.get(i) <= -Peak + (step / 2))
                        quantizedAmplitudeVector.add(-Peak + (step / 2));
                    else if (amplitudeVector.get(i) >= 0) {
                        double quantized = step / 2;
                        while (Math.abs(quantized - amplitudeVector.get(i)) > step / 2) {
                            quantized += step;
                        }
                        quantizedAmplitudeVector.add(quantized);
                    } else {
                        double quantized = -step / 2;
                        while (Math.abs(quantized - amplitudeVector.get(i)) > step / 2) {
                            quantized -= step;
                        }
                        quantizedAmplitudeVector.add(quantized);
                    }
                }
            } else {
                double step = 2 * Peak / Levels;
                for (int i = 0; i < amplitudeVector.size(); i++) {
                    if (amplitudeVector.get(i) > Peak)
                        quantizedAmplitudeVector.add(Peak);
                    else if (amplitudeVector.get(i) < -Peak)
                        quantizedAmplitudeVector.add(-Peak);
                    else if (amplitudeVector.get(i) >= 0) {
                        double quantized = 0;
                        while (Math.abs(quantized - amplitudeVector.get(i)) > step / 2) {
                            quantized += step;
                        }
                        quantizedAmplitudeVector.add(quantized);
                    } else {
                        double quantized = 0;
                        while (Math.abs(quantized - amplitudeVector.get(i)) > step / 2) {
                            quantized -= step;
                        }
                        quantizedAmplitudeVector.add(quantized);
                    }
                }
            }
        }
        double EMS = 0;
        for (int i = 0; i < amplitudeVector.size(); i++)
            EMS += ((amplitudeVector.get(i) - quantizedAmplitudeVector.get(i)) * (amplitudeVector.get(i) - quantizedAmplitudeVector.get(i)));
        EMS = EMS / amplitudeVector.size();
        EMSValue.setText(String.valueOf((double) Math.round(EMS * 1000) / 1000));
        return true;
    }

    private void drawContinuousAndQuantized() {
        graph.removeAllSeries();
        double t, amp;
        if (continuousSignal == null)
            continuousSignal = new LinkedList<>();
        else
            continuousSignal.clear();
        for (int k = 0; k < timeVector.size() - 1; k++) {
            continuousSignal.add(new LineGraphSeries<>());
            double slopeM = (amplitudeVector.get(k + 1) - amplitudeVector.get(k)) / (timeVector.get(k + 1) - timeVector.get(k));
            double constantC = amplitudeVector.get(k) - slopeM * timeVector.get(k);
            int pointsNumber = 100;
            t = timeVector.get(k);
            for (int i = 0; i < pointsNumber; i++) {
                t += (timeVector.get(k + 1) - timeVector.get(k)) / 100;
                amp = slopeM * t + constantC;
                try {
                    continuousSignal.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Draw Continuous", "Error");
                }
            }
            if (k == 0)
                continuousSignal.getLast().setTitle("Continuous Signal");
            graph.addSeries(continuousSignal.getLast());
        }

        if (quantizedSignal == null)
            quantizedSignal = new LinkedList<>();
        else
            quantizedSignal.clear();

        for (int k = 0; k < timeVector.size(); k++) {
            quantizedSignal.add(new LineGraphSeries<>());
            int pointsNumber;
            if (k == 0)
                pointsNumber = (int) (Math.abs(quantizedAmplitudeVector.get(k)) * 100);
            else
                pointsNumber = (int) (Math.abs(quantizedAmplitudeVector.get(k) - quantizedAmplitudeVector.get(k - 1)) * 100);

            t = timeVector.get(k);
            int sign;
            if (quantizedAmplitudeVector.get(k) > 0)
                sign = 1;
            else
                sign = -1;

            double p;
            if (k == 0)
                p = k;
            else
                p = quantizedAmplitudeVector.get(k - 1);

            for (int i = 0; i < pointsNumber; i++) {
                amp = p + 0.01 * i * sign;
                if (!(Math.abs(amp) > Math.abs(quantizedAmplitudeVector.get(k)))) {
                    try {
                        quantizedSignal.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Draw Quantized", "Error");
                        break;
                    }
                }
            }
            if (pointsNumber == 0) {
                quantizedSignal.getLast().appendData(new DataPoint(t - t * 0.01, 0), true, 3);
                quantizedSignal.getLast().appendData(new DataPoint(t, 0), true, 3);
                if (k != timeVector.size() - 1)
                    quantizedSignal.getLast().appendData(new DataPoint(t + t * 0.01, 0), true, 3);
            }

            if (k == 0)
                quantizedSignal.getLast().setTitle("Quantized Signal");

            quantizedSignal.getLast().setColor(Color.RED);
            graph.addSeries(quantizedSignal.getLast());

            //Horizontal
            quantizedSignal.add(new LineGraphSeries<>());
            double d = timeVector.get(1) - timeVector.get(0);
            pointsNumber = (int) (10000 * d) / 100;
            t = timeVector.get(k);
            amp = quantizedAmplitudeVector.get(k);
            for (int i = 0; i < pointsNumber; i++) {
                t += 0.01;
                try {
                    quantizedSignal.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                } catch (Exception e) {
                    Log.d("Draw Quantized", "Error");
                }
            }
            quantizedSignal.getLast().setColor(Color.RED);
            graph.addSeries(quantizedSignal.getLast());

            if (k != timeVector.size() - 1) {
                quantizedSignal.add(new LineGraphSeries<>());
                pointsNumber = (int) (Math.abs(quantizedAmplitudeVector.get(k)) * 100);
                t = timeVector.get(k + 1);
                if (quantizedAmplitudeVector.get(k) > 0)
                    sign = 1;
                else
                    sign = -1;
                for (int i = pointsNumber; i > 0; i--) {
                    amp = 0.01 * i * sign;
                    if (!(Math.abs(amp) < Math.abs(quantizedAmplitudeVector.get(k + 1)))) {
                        try {
                            quantizedSignal.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                        } catch (Exception e) {
                            Log.d("Draw Quantized", "Error");
                            break;
                        }
                    }
                }
                if (pointsNumber == 0) {
                    quantizedSignal.getLast().appendData(new DataPoint(t - t * 0.01, 0), true, 3);
                    quantizedSignal.getLast().appendData(new DataPoint(t, 0), true, 3);
                    if (k != timeVector.size() - 1)
                        quantizedSignal.getLast().appendData(new DataPoint(t + t * 0.01, 0), true, 3);
                }

                if (k == 0)
                    quantizedSignal.getLast().setTitle("Quantized Signal");

                quantizedSignal.getLast().setColor(Color.RED);
                graph.addSeries(quantizedSignal.getLast());
            } else {
                quantizedSignal.add(new LineGraphSeries<>());
                pointsNumber = (int) (Math.abs(quantizedAmplitudeVector.get(k)) * 100);
                t = 2 * timeVector.get(k) - timeVector.get(k - 1);
                if (quantizedAmplitudeVector.get(k) > 0)
                    sign = 1;
                else
                    sign = -1;
                for (int i = 0; i < pointsNumber; i++) {
                    amp = 0.01 * i * sign;
                    try {
                        quantizedSignal.getLast().appendData(new DataPoint(t, amp), true, pointsNumber);
                    } catch (Exception e) {
                        Log.d("Draw Quantized", "Error");
                        break;
                    }
                }
                if (pointsNumber == 0) {
                    quantizedSignal.getLast().appendData(new DataPoint(t - t * 0.01, 0), true, 2);
                    quantizedSignal.getLast().appendData(new DataPoint(t, 0), true, 2);
                    if (k != timeVector.size() - 1)
                        quantizedSignal.getLast().appendData(new DataPoint(t + t * 0.01, 0), true, 3);
                }
                quantizedSignal.getLast().setColor(Color.RED);
                graph.addSeries(quantizedSignal.getLast());
            }
        }
        graph.getGridLabelRenderer().setHorizontalAxisTitle(xTitle);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Volts");
        graph.setTitle("Continuous Signal (Blue) & Quantized Samples (Red)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Volts");
    }
}