package io.phonk.runner.apprunner.api.media.midi;


import android.media.midi.MidiDeviceInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MidiOutput {
    /**
     * Unique id of a device output. If multiple devices are available, this id will always be unique
     */
    private final String deviceOutputId;

    /**
     * A human readable name for the midi output
     */
    private final String name;

    /**
     * Device id
     */
    private final int deviceId;

    /**
     * The port number for a given midi device
     */
    private final int portNumber;

    public MidiOutput(final MidiDeviceInfo midiDeviceInfo, final int portNumber) {
        this.deviceOutputId = midiDeviceInfo.getId() + "-" + portNumber;
        this.name = computeMidiOutputName(midiDeviceInfo, portNumber);
        this.deviceId = midiDeviceInfo.getId();
        this.portNumber = portNumber;
    }

    private String computeMidiOutputName(final MidiDeviceInfo midiDeviceInfo, final int portNumber) {
        final MidiDeviceInfo.PortInfo portInfo = midiDeviceInfo.getPorts()[portNumber];
        final String portName = portInfo != null ? portInfo.getName() : null;
        return "#" + deviceId
                + ", " + getDescription(midiDeviceInfo)
                + "[" + portNumber + "]"
                + ", " + portName;
    }

    private String getDescription(final MidiDeviceInfo midiDeviceInfo) {
        final String description = midiDeviceInfo.getProperties()
                .getString(MidiDeviceInfo.PROPERTY_NAME);
        if (description == null) {
            return midiDeviceInfo.getProperties()
                    .getString(MidiDeviceInfo.PROPERTY_MANUFACTURER) + ", "
                    + midiDeviceInfo.getProperties()
                    .getString(MidiDeviceInfo.PROPERTY_PRODUCT);
        }
        return description;
    }

    public String getDeviceOutputId() {
        return deviceOutputId;
    }

    public String getName() {
        return name;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getPortNumber() {
        return portNumber;
    }
}
