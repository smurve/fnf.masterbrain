package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by tho on 13.08.2015.
 */
public class ScoreDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreDao.class);
    private boolean enabled = true;
    private File dataDir = new File("data/tot");
    private ObjectMapper mapper = new ObjectMapper();

    public void storeScore(ScoredGenom<TOTGenom> result) {
        if (enabled) {
            dataDir.mkdirs();
            TOTGenom genom = result.getGenom();

            File file = resolveFileName(genom);

            Score s = new Score();
            s.setScore(result.getScore());

            try (OutputStream out = new FileOutputStream(file)) {
                mapper.writeValue(out, s);
            } catch (Exception e) {
                LOGGER.error("Failed to write score.", e);
            }
        }
    }

    private File resolveFileName(TOTGenom genom) {
        StringBuilder buf = new StringBuilder();
        for (double d : genom.getValues()) {
            if (d < 0) {
                buf.append('n');
            }
            buf.append(Math.abs(d)).append('_');
        }
        String fileName = buf.toString().substring(0, buf.length() - 1) + ".json";
        return new File(dataDir, fileName);
    }

    public void storeBestScore(ScoredGenom<TOTGenom> result) {
        if (enabled) {
            dataDir.mkdirs();
            File file = new File(dataDir, "best_score_" + StringUtils.leftPad("" + (long) result.getScore(), 10, "0") + ".json");
            try (OutputStream out = new FileOutputStream(file)) {
                mapper.writeValue(out, result.getGenom());
            } catch (Exception e) {
                LOGGER.error("Failed to write score.", e);
            }
        }
    }

    public ScoredGenom<TOTGenom> loadBestScore() {
        String[] fileNames = dataDir.list((dir, name) -> StringUtils.startsWith(name, "best_score_") && StringUtils.endsWith(name, ".json"));

        if (fileNames.length == 0) {
            LOGGER.info("No best score found.");
            return null;
        }
        List<String> files = Arrays.asList(fileNames);
        Collections.sort(files);
        String fileName = files.get(0);
        File file = new File(dataDir, fileName);
        LOGGER.info("Best file is: {}", file.getAbsolutePath());
        long score = 0;
        try {
            score = Long.valueOf(StringUtils.substringBetween(fileName, "best_score_", ".json"));
        } catch (Exception e) {
            LOGGER.error("Failed to extract score from file name");
        }
        try (InputStream in = new FileInputStream(file)) {
            TOTGenom genom = mapper.readValue(in, TOTGenom.class);
            ScoredGenom<TOTGenom> sg = new ScoredGenom<>(genom, score);
            return sg;
        } catch (Exception e) {
            LOGGER.error("Failed to read best score.", e);
        }
        return null;

    }


    public ScoredGenom<TOTGenom> loadExistingScore(TOTGenom genom, Configuration configuration) {
        ScoredGenom<TOTGenom> scoredGenom = null;
        if (enabled) {
            File file = resolveFileName(genom);
            if (file.exists()) {
                LOGGER.info("reading existing score from file={}", file.getAbsolutePath());
                try (InputStream in = new FileInputStream(file)) {
                    Score score = mapper.readValue(in, Score.class);
                    scoredGenom = new ScoredGenom<>(genom, score.getScore());
                } catch (Exception e) {
                    LOGGER.error("Failed to read score.", e);
                }
            } else {
                LOGGER.debug("No score found for genom={}", genom);
            }
        }
        return scoredGenom;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    static class Score {
        private double score;

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}
