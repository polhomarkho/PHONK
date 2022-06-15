/*
 * Part of Phonk http://www.phonk.io
 * A prototyping platform for Android devices
 *
 * Copyright (C) 2013 - 2017 Victor Diaz Barrales @victordiaz (Protocoder)
 * Copyright (C) 2017 - Victor Diaz Barrales @victordiaz (Phonk)
 *
 * Phonk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Phonk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Phonk. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.phonk.runner.apprunner.api.media.midi;

import static android.content.Context.MIDI_SERVICE;

import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apidoc.annotation.PhonkMethod;
import io.phonk.runner.apidoc.annotation.PhonkMethodParam;
import io.phonk.runner.apprunner.AppRunner;
import io.phonk.runner.apprunner.api.ProtoBase;
import io.phonk.runner.apprunner.api.widgets.midi.CcSlider;
import io.phonk.runner.apprunner.api.widgets.midi.MidiUiFactory;
import io.phonk.runner.base.utils.MLog;

@RequiresApi(api = Build.VERSION_CODES.M)
@PhonkClass
public class PMidiController extends ProtoBase {
    private static final String TAG = PMidiController.class.getSimpleName();

    public static final int STATUS_NOTE_OFF = 0x80;
    public static final int STATUS_NOTE_ON = 0x90;
    public static final int STATUS_PITCH_BEND = 0xE0;
    public static final int STATUS_CC = 0xB0;
    public static final int DISABLE_PITCH_BEND_VALUE = 8192;  // max pitch bend (=16383) / 2
    public static final int MIN_PITCH_BEND_VALUE = 0;
    public static final int MAX_PITCH_BEND_VALUE = 16383;
    public static final int MIN_CC_VALUE = 0;
    public static final int MAX_CC_VALUE = 127;
    public static final int MAX_VELOCITY_VALUE = 127;
    private int bpmCounter = 0;
    private long previousQuarterNoteTime = 0;
    private long previousQuarterNoteTime2 = 0;

    public MidiUiFactory ui;
    public MidiNotePlayer player;
    private MidiManager midiManager;
    private MidiInputPort midiInputPort;
    private MidiOutputPort midiOutputPort;
    private MidiDevice midiDevice;
    private final Map<String, MidiInput> midiInputByIds = new TreeMap<>();
    private final Map<String, MidiOutput> midiOutputByIds = new TreeMap<>();
    private final Map<Integer, MidiDeviceInfo> midiDeviceInfoByIds = new HashMap<>();
    public final Map<Integer, CcSlider> ccSliders = new HashMap<>();  // sera à déplacer

//    class MidiReceiver extends MidiReceiver {
//        public void onSend(byte[] data, int offset,
//                           int count, long timestamp) throws IOException {
//            // parse MIDI or whatever
//        }
//    }


    public PMidiController(final AppRunner appRunner) {
        super(appRunner);
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            midiManager = (MidiManager) getContext().getSystemService(MIDI_SERVICE);
            if (midiManager != null) {
                return;
            }
        }
        Toast.makeText(getContext(), "MIDI not supported!", Toast.LENGTH_LONG)
                .show();
    }

    @PhonkMethod(description = "Find available midi inputs to send midi command to", example = "")
    @PhonkMethodParam(params = "")
    public MidiInput[] findAvailableMidiInputs() {
        midiInputByIds.clear();
        midiDeviceInfoByIds.clear();
        for (final MidiDeviceInfo midiDeviceInfo : midiManager.getDevices()) {
            midiDeviceInfoByIds.put(midiDeviceInfo.getId(), midiDeviceInfo);
            final int inputPortCount = midiDeviceInfo.getInputPortCount();
            for (int portNumber = 0; portNumber < inputPortCount; portNumber++) {
                final MidiInput midiInput = new MidiInput(midiDeviceInfo, portNumber);
                midiInputByIds.put(midiInput.getDeviceInputId(), midiInput);
            }
        }
        return midiInputByIds.values().toArray(new MidiInput[0]);
    }

    @PhonkMethod(description = "Find available midi outputs to receive midi command", example = "")
    @PhonkMethodParam(params = "")
    public MidiOutput[] findAvailableMidiOutputs() {
        midiOutputByIds.clear();
        midiDeviceInfoByIds.clear();
        for (final MidiDeviceInfo midiDeviceInfo : midiManager.getDevices()) {
            midiDeviceInfoByIds.put(midiDeviceInfo.getId(), midiDeviceInfo);
            final int outputPortCount = midiDeviceInfo.getOutputPortCount();
            for (int portNumber = 0; portNumber < outputPortCount; portNumber++) {
                final MidiOutput midiOutput = new MidiOutput(midiDeviceInfo, portNumber);
                midiOutputByIds.put(midiOutput.getDeviceOutputId(), midiOutput);
            }
        }
        return midiOutputByIds.values().toArray(new MidiOutput[0]);
    }

    @PhonkMethod(description = "Find available midi inputs to send midi command to", example = "")
    @PhonkMethodParam(params = "midiInputId fetched from findAvailableMidiInputs()")
    public PMidiController setupMidi(final String midiInputId, final String midiOutputId) {
        // input
        final MidiInput midiInput = midiInputByIds.get(midiInputId);
        if (midiInput != null) {
            final MidiDeviceInfo midiDeviceInfo = midiDeviceInfoByIds.get(midiInput.getDeviceId());
            midiManager.openDevice(midiDeviceInfo, midiDevice -> {
                this.midiDevice = midiDevice;
                if (midiDevice == null) {
                    Log.e(TAG, "could not open device " + midiInput);
                } else {
                    midiInputPort = midiDevice.openInputPort(midiInput.getPortNumber());
                }
            }, new Handler(Looper.getMainLooper()));
        } else {
            Toast.makeText(getContext(), "Unknown MIDI input id: " + midiInputId, Toast.LENGTH_LONG)
                    .show();
        }
        // output
        if (midiOutputId != null) {
            final MidiOutput midiOutput = midiOutputByIds.get(midiOutputId);
            if (midiOutput != null) {
                final MidiDeviceInfo midiDeviceInfo = midiDeviceInfoByIds.get(midiOutput.getDeviceId());
                midiManager.openDevice(midiDeviceInfo, midiDevice -> {
                    this.midiDevice = midiDevice;
                    if (midiDevice == null) {
                        Log.e(TAG, "could not open device " + midiOutput);
                    } else {
                        midiOutputPort = midiDevice.openOutputPort(midiOutput.getPortNumber());
                        if (midiOutputPort == null) {
                            Log.e(TAG, "could not open output port " + midiOutputPort);
                        } else {
                            midiOutputPort.connect(new MidiReceiver() {
                                @Override
                                public void onSend(final byte[] msg, final int offset, final int count, final long timestamp) throws IOException {
                                    final int msgAsInt = msg[1] & 0xFF;
                                    if (msgAsInt < 0xF8) {
                                        final int status = msg[1] & 0xF0;
                                        final int channel = msg[1] & 0x0F;
                                        // maj des sliders OK
                                        if (status == STATUS_CC) {
                                            final int cc = msg[2] & 0xFF;
                                            final int value = msg[3] & 0xFF;
                                            final CcSlider ccSlider = ccSliders.get(STATUS_CC + channel + cc);
                                            if (ccSlider != null) {
                                                getActivity().runOnUiThread(() -> ccSlider.slider().valueAndTriggerEvent(value));
                                            }
                                        }
                                    } else if (msgAsInt == 0xF8) {  // FIXME doesn't seem responsive enough (lag)
                                        bpmCounter++;
                                        if (bpmCounter == 24) {
//                                            final long time = new Date().getTime();
//                                            long timeDelta = time - previousQuarterNoteTime;
//                                            previousQuarterNoteTime = time;
                                            long timeDelta2 = timestamp - previousQuarterNoteTime2;
                                            previousQuarterNoteTime2 = timestamp;
//                                            Log.i("RECEIVER", "BPM : " + 60000 / timeDelta + " (" + timeDelta + ")");
                                            Log.i("RECEIVER", "BPM2 : " + 60000000000L / timeDelta2 + " (" + timeDelta2 + ")");
                                            bpmCounter = 0;
                                        }
                                    } else {
                                        Log.i("RECEIVER", "received : " + Arrays.toString(msg));
                                    }
                                }
                            });
                        }
                    }
                }, new Handler(Looper.getMainLooper()));
            } else {
                Toast.makeText(getContext(), "Unknown MIDI output id: " + midiOutputId, Toast.LENGTH_LONG)
                        .show();
            }
        }
        ui = new MidiUiFactory(this);
        player = new MidiNotePlayer(this);
        return this;
    }

    @PhonkMethod(description = "Plays a note", example = "")
    @PhonkMethodParam(params = "channel [0-15], pitch[0-127], velocity[0-127]")
    public void noteOn(final int channel, final int pitch, final int velocity) {
        midiCommand(STATUS_NOTE_ON + channel, pitch, velocity);
    }

    @PhonkMethod(description = "Stop playing a note", example = "")
    @PhonkMethodParam(params = "channel [0-15], pitch[0-127], velocity[0-127]")
    public void noteOff(final int channel, final int pitch, final int velocity) {
        midiCommand(STATUS_NOTE_OFF + channel, pitch, velocity);
    }

    @PhonkMethod(description = "Plays a note", example = "")
    @PhonkMethodParam(params = "channel [0-15], noteName['C4', 'C#3', 'Eb4'...], velocity[0-127]")
    public void noteOn(final int channel, final String noteName, final int velocity) {
        final int pitch = Note.getPitch(noteName);
        midiCommand(STATUS_NOTE_ON + channel, pitch, velocity);
    }

    @PhonkMethod(description = "Stop playing a note", example = "")
    @PhonkMethodParam(params = "channel [0-15], noteName['C4', 'C#3', 'Eb4'...], velocity[0-127]")
    public void noteOff(final int channel, final String noteName, final int velocity) {
        final int pitch = Note.getPitch(noteName);
        midiCommand(STATUS_NOTE_OFF + channel, pitch, velocity);
    }

    @PhonkMethod(description = "Pitch bend notes", example = "")
    @PhonkMethodParam(params = "channel [0-15], pitchBendValue[0-16383] with 8192 being no pitch bend")
    public void pitchBend(final int channel, final int pitchBendValue) {
        // need to convert pitchBendValue in lsb 7 bytes and msb 7 bytes
        int pitchBendValueMsbOn7Bytes = pitchBendValue >> 7;
        int pitchBendValueLsbOn7Bytes = pitchBendValue & 0x7F;
        // careful, lsb is first!
        midiCommand(STATUS_PITCH_BEND + channel, pitchBendValueLsbOn7Bytes, pitchBendValueMsbOn7Bytes);
    }

    @PhonkMethod(description = "Disable pitch bend", example = "")
    @PhonkMethodParam(params = "channel [0-15]")
    public void stopPitchBend(final int channel) {
        pitchBend(channel, DISABLE_PITCH_BEND_VALUE);
    }

    @PhonkMethod(description = "Sends a midi message you build yourself. Quite low level!", example = "")
    @PhonkMethodParam(params = "status [0-127], data1[0-127], data2[0-127]")
    public void midiCommand(final int status, final int data1, final int data2) {
        final byte[] mByteBuffer = new byte[3];
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        mByteBuffer[2] = (byte) data2;
        long now = System.nanoTime();
        midiSend(mByteBuffer, now);
    }

    private void midiSend(final byte[] buffer, final long timestamp) {
        try {
            if (midiInputPort != null) {
                midiInputPort.send(buffer, 0, buffer.length, timestamp);
            }
        } catch (IOException e) {
            Log.e(TAG, "midiSend failed " + e);
        }
    }

    @Override
    public void __stop() {
        MLog.i(TAG, "close");
        try {
            // clean input
            if (midiInputPort != null) {
                midiInputPort.close();
            }
            midiInputPort = null;
            // clean output
            if (midiOutputPort != null) {
                midiOutputPort.close();//disconnect(mDispatcher);
            }
            midiOutputPort = null;
            if (midiDevice != null) {
                midiDevice.close();
            }
            midiDevice = null;
        } catch (IOException e) {
            Log.e(TAG, "midi cleanup failed", e);
        }
    }
}
