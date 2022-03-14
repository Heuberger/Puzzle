/*
 * Jigsaw Puzzle - Copyright: Carlos F. Heuberger. All rights reserved.
 */

package cfh.puzzle;

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * @author Carlos F. Heuberger
 */
public class Sound {

    private final Executor executor;
    
    private final Sample pieceJoin;
    private final Sample pieceDisconnect;
    private final Sample groupSelect;
    private final Sample groupCopy;
    private final Sample posMark;
    private final Sample posSet;
    private final Sample complete;
    
    Sound() {
        executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        
        pieceJoin = load("resources/pieceJoin.wav");
        pieceDisconnect = load("resources/pieceDisconnect.wav");
        groupSelect = load("resources/groupSelect.wav");
        groupCopy = load("resources/groupCopy.wav");
        posMark = load("resources/posMark.wav");
        posSet = load("resources/posSet.wav");
        complete = load("resources/complete.wav");
    }
    
    public void pieceJoin() {
        play(pieceJoin);
    }
    
    public void pieceDisconnect() {
        play(pieceDisconnect);
    }
    
    public void groupSelect() {
        play(groupSelect);
    }
    
    public void groupCopy() {
        play(groupCopy);
    }

    public void posMark() {
        play(posMark);
    }
    
    public void posSet() {
        play(posSet);
    }
    
    public void complete() {
        play(complete);
    }

    // TODO thread?
    private void play(Sample sample) {
        executor.execute(() -> {
            if (sample == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                try (SourceDataLine line = AudioSystem.getSourceDataLine(sample.format)) {
                    line.open();
                    line.start();
                    line.write(sample.data, 0, sample.data.length);
                    line.drain();
                } catch (LineUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private Sample load(String location) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        URL url = getClass().getResource(location);
        try (AudioInputStream input = AudioSystem.getAudioInputStream(url)) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = input.read(buffer)) != -1) {
                data.write(buffer, 0, count);
            }
            return new Sample(input.getFormat(), data.toByteArray());
        } catch (NullPointerException | IOException | UnsupportedAudioFileException ex) {
            System.err.printf("%s: Loading %s%n", ex.getClass().getSimpleName(), location);
            ex.printStackTrace();
            return null;
        }
    }
    
    private static class Sample {
        private final AudioFormat format;
        private final byte[] data;
        Sample(AudioFormat format, byte[] data) {
            this.format = format;
            this.data = data;
        }
    }
}
