package io.phonk.runner.apprunner.api.widgets.midi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apidoc.annotation.PhonkMethod;
import io.phonk.runner.apprunner.api.common.ReturnObject;
import io.phonk.runner.apprunner.api.media.midi.PMidiController;
import io.phonk.runner.apprunner.api.widgets.PSlider;

@RequiresApi(api = Build.VERSION_CODES.M)
@PhonkClass
public class PitchBendSlider extends AbstractMidiWidget {

    private final PSlider pSlider;
    private final PMidiController pMidiController;
    private int channel = 0;

    public PitchBendSlider(final PMidiController pMidiController,
                           final Object x,
                           final Object y,
                           final Object w,
                           final Object h) {
        super(pMidiController.getAppRunner());
        this.pMidiController = pMidiController;
        pSlider = getAppRunner().pUi
                .addSlider(x, y, w, h)
                .range(PMidiController.MIN_PITCH_BEND_VALUE, PMidiController.MAX_PITCH_BEND_VALUE)
                .value(PMidiController.DISABLE_PITCH_BEND_VALUE)
                .verticalMode(true)
                .mode("drag")
                .text("bend")
                .onChange(this::doPitchBend)
                .onRelease(this::stopPitchBend);
    }

    @PhonkMethod()
    public PitchBendSlider withChannel(final int channel) {
        this.channel = channel;
        return this;
    }

    @PhonkMethod()
    public PitchBendSlider withText(final String text) {
        pSlider.text(text);
        return this;
    }

    @PhonkMethod()
    public PSlider slider() {
        return pSlider;
    }

    private void doPitchBend(final ReturnObject r) {
        pMidiController.pitchBend(channel, numberToInt(r.get("value")));
    }

    private void stopPitchBend(final ReturnObject r) {
        pSlider.valueAndTriggerEvent(8192);
        pMidiController.stopPitchBend(channel);
    }

    @Override
    public void __stop() {

    }
}
