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
public class CcSlider extends AbstractMidiWidget {

    private final PSlider pSlider;
    private final PMidiController pMidiController;
    private int channel = 0;
    private int cc;

    public CcSlider(final PMidiController pMidiController,
                    final Object x,
                    final Object y,
                    final Object w,
                    final Object h,
                    final int cc) {
        super(pMidiController.getAppRunner());
        this.pMidiController = pMidiController;
        this.cc = cc;
        pSlider = getAppRunner().pUi
                .addSlider(x, y, w, h)
                .range(PMidiController.MIN_CC_VALUE, PMidiController.MAX_CC_VALUE)
                .verticalMode(true)
                .mode("drag")
                .onChange(this::midiAction);
    }

    @PhonkMethod()
    public CcSlider withChannel(final int channel) {
        this.channel = channel;
        return this;
    }

    @PhonkMethod()
    public CcSlider withText(final String text) {
        pSlider.text(text);
        return this;
    }

    @PhonkMethod()
    public CcSlider withCc(final int cc) {
        this.cc = cc;
        return this;
    }

    @PhonkMethod()
    public PSlider slider() {
        return pSlider;
    }

    private void midiAction(final ReturnObject r) {
        pMidiController.midiCommand(PMidiController.STATUS_CC + channel, cc, numberToInt(r.get("value")));
    }

    @Override
    public void __stop() {

    }
}
