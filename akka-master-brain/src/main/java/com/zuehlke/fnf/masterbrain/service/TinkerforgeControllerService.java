package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import com.tinkerforge.BrickletLCD20x4;
import com.tinkerforge.BrickletLinearPoti;
import com.tinkerforge.IPConnection;
import com.zuehlke.fnf.masterbrain.akka.Publisher;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by tho on 08.09.2015.
 */
@Service
public class TinkerforgeControllerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TinkerforgeControllerService.class);
    private static final String HOST = "localhost";
    private static final int PORT = 4223;
    private static final String POTI_UID = "72Y"; // Change to your UID
    private static final String LCD_UID = "boo"; // Change to your UID
    @Autowired
    public MasterBrainService masterBrain;
    private IPConnection ipcon = new IPConnection();
    private BrickletLinearPoti lp;
    private BrickletLCD20x4 lcd;

    @PostConstruct
    public void init() {
        try {
            ipcon.connect(HOST, PORT);

            lp = new BrickletLinearPoti(POTI_UID, ipcon);
            lcd = new BrickletLCD20x4(LCD_UID, ipcon);

            lcd.backlightOn();
            lcd.clearDisplay();

            lp.setPositionCallbackPeriod(50);
            BrickletLinearPoti.PositionListener positionListener = position -> {
                int target = (int) ((double) position / (double) 100 * (double) 255);
                try {
                    lcd.writeLine((short) 0, (short) 0, String.format(
                            "Raw=%3s, Power=%3s", position, target));
                } catch (Exception e) {
                    // ignore
                }
                Publisher.tell(Power.of(target), masterBrain.getPilot(), ActorRef.noSender()).onMissingSender().ignore().andReturn();
            };
            lp.addPositionListener(positionListener);
            positionListener.position(lp.getPosition());
        } catch (Exception e) {
            LOGGER.info("Controller failed. Is it connected? Error={}", e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (lcd != null) {
                lcd.backlightOff();
            }
            ipcon.disconnect();
        } catch (Exception e) {
            LOGGER.info("Shudown error={}", e.getMessage());
        }
    }
}
