package mlsystem.toggler;

import java.io.IOException;
import java.security.ProviderException;
import java.time.Duration;

import org.apache.logging.log4j.Logger;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

import mlsystem.shared.action.ActionWorker;

public class GPIOToggler extends ActionWorker {
    private Context pi4j;
    private DigitalOutput toggler;
    private final int PIN_TO_TOGGLE;

    public GPIOToggler(int pinToToggle, Duration interval, Logger logger) throws IOException {
        super("GPIOTogglerWorker","GPIOToggler worker was interrupted",interval,logger);
        PIN_TO_TOGGLE = pinToToggle;
    }

    @Override
    protected void Work() {
        toggler.toggle();
    }

    @Override
    public void Start() throws ProviderException {
        pi4j = Pi4J.newAutoContext();
        toggler = pi4j.dout().create(PIN_TO_TOGGLE);
        toggler.config().shutdownState(DigitalState.LOW);

        super.Start();
    }

    @Override
    public void Stop() {
        super.Stop();

        pi4j.shutdown();
    }
    
}
