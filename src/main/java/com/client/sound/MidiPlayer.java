package com.client.sound;

import javax.sound.midi.*;
import java.io.ByteArrayInputStream;

/**
 * Very small MIDI player for the 317-style cache index3 music tracks.
 *
 * Goal for v1:
 *  - Play the decompressed MIDI bytes the moment they arrive from the on-demand fetcher
 *  - Never crash the game if the host machine has no MIDI device
 *  - Allow changing volume at runtime
 *
 * This intentionally does NOT try to perfectly emulate RuneScape's original MIDI synth yet.
 * It's a solid foundation so you can later add:
 *  - SoundFont (SF2) support for authentic instruments
 *  - Fade in/out and crossfades (songChanging)
 *  - Jingles + queued tracks
 */
public final class MidiPlayer {

    private static final MidiPlayer INSTANCE = new MidiPlayer();

    public static MidiPlayer get() {
        return INSTANCE;
    }

    private Sequencer sequencer;
    private Synthesizer synth;
    private int currentTrackId = -1;
    private int volume0To255 = 255;

    private MidiPlayer() {}

    private void ensureOpen() throws MidiUnavailableException {
        if (synth == null || !synth.isOpen()) {
            synth = MidiSystem.getSynthesizer();
            synth.open();
        }
        if (sequencer == null || !sequencer.isOpen()) {
            // false = don't auto-connect to default synth, we wire it ourselves
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();

            // Route sequencer -> synth
            Transmitter transmitter = sequencer.getTransmitter();
            Receiver receiver = synth.getReceiver();
            transmitter.setReceiver(receiver);
        }
    }

    /**
     * Play a MIDI track from bytes.
     *
     * @param trackId track id (cache file id) purely for "current track" bookkeeping
     * @param midiBytes decompressed .mid bytes (usually starts with "MThd")
     * @param loop true to loop forever
     * @param volume0To255 client music volume (0..255)
     */
    public synchronized void play(int trackId, byte[] midiBytes, boolean loop, int volume0To255) {
        if (midiBytes == null || midiBytes.length == 0) {
            return;
        }

        try {
            ensureOpen();

            Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(midiBytes));
            sequencer.stop();
            sequencer.setSequence(sequence);

            this.currentTrackId = trackId;
            setVolume(volume0To255);

            sequencer.setLoopCount(loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
            sequencer.setTickPosition(0);
            sequencer.start();
        } catch (Throwable t) {
            // Never let audio break the client.
            // Common reasons:
            //  - No MIDI synthesizer available
            //  - Headless / minimal JRE
            //  - Broken OS audio device
            stop();
        }
    }

    public synchronized void stop() {
        try {
            if (sequencer != null) {
                sequencer.stop();
            }
        } catch (Throwable ignored) {}
        currentTrackId = -1;
    }

    /**
     * Set volume at runtime.
     * 0..255 will be mapped to MIDI CC 7 range 0..127.
     */
    public synchronized void setVolume(int volume0To255) {
        this.volume0To255 = Math.max(0, Math.min(255, volume0To255));
        int vol = (this.volume0To255 * 127) / 255;

        try {
            if (synth != null && synth.isOpen()) {
                MidiChannel[] channels = synth.getChannels();
                if (channels != null) {
                    for (MidiChannel ch : channels) {
                        if (ch != null) {
                            ch.controlChange(7, vol); // Channel volume
                            ch.controlChange(11, vol); // Expression (helps on some synths)
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    public synchronized int getCurrentTrackId() {
        return currentTrackId;
    }
}
