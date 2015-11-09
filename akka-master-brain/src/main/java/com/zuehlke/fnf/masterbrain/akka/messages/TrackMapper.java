package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import org.apache.commons.beanutils.BeanUtils;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by mhan on 06.07.2015.
 */
public class TrackMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Track fromRObject(REXP o) throws REXPMismatchException {
        Track toReturn = new Track();

        HashMap<HashMap, String> o1 = (HashMap<HashMap, String>) o.asNativeJavaObject();
        o1.forEach((m, s) -> {
            if ("course".equals(s)) {
                readCourse(m, toReturn);
            } else if ("gyro_profile".equals(s)) {
                readLocations(m, toReturn);
            } else if ("status".equals(s)) {
                StatusMapper.readStatus(m, (status) -> toReturn.setStatus(status));
            }
        });
        return toReturn;
    }

    private static void readLocations(HashMap<double[], Object> m, Track toReturn) {
        double[] xs = null;
        double[] ys = null;
        double[] index = null;
        double[] gs = null;

        for (double[] key : m.keySet()) {
            Object name = m.get(key);
            if ("x".equals(name)) {
                xs = key;
            } else if ("y".equals(name)) {
                ys = key;
            } else if ("ind".equals(name)) {
                index = key;
            } else if ("g".equals(name)) {
                gs = key;
            }
        }

        toReturn.setTrackpoints(TrackPoints.from(xs, ys, gs, index));

    }

    private static void readCourse(final HashMap<HashMap, Object> m, Track toReturn) {
        m.forEach((m2, s) -> {
            if ("arcs".equals(s)) {
                TrackMapper.readSensorData(m2, Curve::new, toReturn::setCurves);
            }
            if ("straights".equals(s)) {
                TrackMapper.readSensorData(m2, Straight::new, toReturn::setStraights);
            }
        });
    }

    private static <T> void readSensorData(final HashMap<Object, String> m2, Supplier<T> supp, Consumer<List<T>> consumer) {
        List<T> objectList = new ArrayList<>();
        m2.forEach((m3, propertyName) -> {


            if (m3.getClass().getComponentType() == int.class) {
                readDoubleArray(supp, objectList, propertyName, toDoubleArray((int[]) m3), o -> new int[]{o.intValue()});
            }
            if (m3.getClass().getComponentType() == double.class) {
                readDoubleArray(supp, objectList, propertyName, (double[]) m3, o -> new double[]{o});
            }
        });
        consumer.accept(objectList);
    }

    private static double[] toDoubleArray(final int[] m3) {
        return IntStream.of(m3).mapToDouble(o -> (double) o).toArray();
    }

    private static <T, E> void readDoubleArray(final Supplier<T> supp, final List<T> objectList, final String propertyName, final double[] m4,
                                               Function<Double, E> toDo) {
        for (int i = 0; i < m4.length; i++) {
            T currentStraight = getStraight(objectList, i, supp);
            try {
                BeanUtils.setProperty(currentStraight, propertyName, toDo.apply(m4[i]));
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
    }

    private static <T> T getStraight(final List<T> straightList, final int i, final Supplier<T> supp) {
        if (i < straightList.size()) {
            return straightList.get(i);
        }
        T straight = supp.get();
        straightList.add(straight);
        return straight;
    }


    public static String toJSON(Track track) throws JsonProcessingException {
        return mapper.writeValueAsString(track);
    }

    public static Track fromJSON(String track) throws IOException {
        return mapper.readValue(track, Track.class);
    }

    public static String toJSON(TrackWithPowerProfile trackWithPowerProfile) throws JsonProcessingException {
        return mapper.writeValueAsString(trackWithPowerProfile);
    }
}
