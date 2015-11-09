# Show Case

## Zlot-Cars Project by the Data Analytics Team

The data analytics team implemented a pilot framework for the [Fast and Furious](https://github.com/FastAndFurious) toolbox done by Wolfgang Giersche.

### Fast and Furious

*by Wolfgang Giersche*

Fast and Furious consists out of:

**Hardware:**
- Racing car with a 9 DOF (Degrees of Freedom, see appendix) sensor (Arduino with Bluetooth module)
    - Why bluetooth and not XBee? The XBee module doesn't fit into the car.
- Speed Traps (Arduinos with XBee modules)
- Race track access point (Arduino with XBee)

**Software:**
- Relay for communication with race track
- Simulator for testing the pilots (maybe not available in that setup)
- Team and competition management (http://192.168.0.61:8082)
- A control console for track configuration
- Configuration server
- a Starter Kit for easy setup
- RabbitMQ for communication
- maybe some more... only Wolfgang knows :-)

### Akka-Master-Brain

The pilot implemented by the data analytics team is actually a multi-pilot with some goodies:

- Switchable pilot algorithms
- Track recognition for all algorithms
- Localization for all algrorithms
- Manual control feature
- Visualization in a browser

**Why visualization?**

Not a mandatory feature for implementing a pilot but it helps debugging as you can see your data in diagrams in real
time. Getting that information from log files is quite cumbersome. Next, some graphs and animations are nice and
can be used to explain boring data to non analytics folks!

**Why master brain**

As we control much more than just one pilot algorithm we found that this is a good, stupid enough name :-)

#### Technology

**Server**
- Spring Boot: for bootstrapping the webserver, akka and communication with the FnF servers
- Akka: message-drive framework for data processing
- RabbitMQ: for communication with simulator or relay server (race track)

**Frontend**
- AngularJS Material: responsive webapp
- D3.js: Graphs and Track
- Stomp (Websocket): for pushing data from the server to the browser

More details can be found in the file [akka-master-brain/README.md](../akka-master-brain/README.md).

Admin guide: [main README.md](../README.md).

## Algorithms

### Manual

Manual control. Either use slider in browser or hardware control if available.

### Power Profile Pilot

*by Marcel Hanser*

A pilot that trains a power profile. The power profile contains out of power values for every point on the track.
This algorithm trains using a genetic algorithm.

#### Inital Population
Create a random value for each segment of the track (The colorful dot's in the webapp).

#### Evaluation

The evaluation phase is done by running an internal simulation for driving a lap for every item of the population.

#### Fitness

The calculated lap time is used as the fitness value.

#### Pairing and mutation

The population gets resampled by pairing some random items and by randomly mutate some values. The pairing is not fully
random because successful configurations get a higher weight.

#### Current configuration

```
learner {
		gaInstances=7
		iterations=500
		populationSize=250
		segmentsPerTrack=80
}
```

This algorithm runs for about 30 seconds. It takes a huge amount of cpu power. It runs the algorithm with 7 independent
instances in parallel. Each instance has a population of 250 with 80 variables per population item. It runs 500 interations.

**The result is very bad! :-)** Maybe some configuration tweaking would help.

### THO - Thomas
*by Thomas Scheuchzer*

The algorithm doesn't involve much data analytics. It simply looks at some locations
in the future, counts how many curve segments are ahead and decides what to do:

- only straights ahead: Full Power (255)
- curve ahead: Fixed break power (160)
- reaching end of curve: Full Power (255)

The algorithm switches into a safety mode (Power 160) if localization is bad or we ran into a penalty. It stays in safety
mode until localization has been restored.

In case of a penalty the algorithm marks some straight segments as curves. This update of the track is not visible in the webapp.

### Train on Track (Autopilot)
*by Gian-Marco Bashera, Thomas Scheuchzer*

This algorithm uses the result of the `Train on Track (Learner)`.

**Note that you can train on one track layout, build another track layout and then directly drive on the new track without
restarting the learning process!**

As long as the physical properties of the racing track are similar the algorithm can handle it.

After loading the algorithm it does some adjustments to adapt to the new track layout. The webapp shows the field `adjust.power.factor`
where you see the current adjustment. You can change that value manually but you have to be fast as the algorithm might
update it after every lap.

#### (manual) Configuration

The algorithm loads the configuration/genom from the directory `data/tot`. It loads the first `best_score_*.json`.
The files are loaded in ascending order, therefore the first file is the best configuration.

If no good best_score file is available and there's no time for learning you can manually copy a file from the directory
`good_pilots` to the `data/tot/` directory. Switch the pilot to manual and then back to the auto pilot.

If the learner messed it up and create some bad `best_score` files with impossibly low scores (say 4000 milliseconds) you can
safely delete them from the disc. Sometimes we have some problems with detecting when a round is finished during evaluation. This
is mostly due to localization problem where the current location jumps wildly across the track.

### Train on Track (Learner)

*by Gian-Marco Bashera, Thomas Scheuchzer*

A pilot that trains a neuronal network with 3 variables. The training is done with a genetic algorithm (it's the same
framework for the genetic algorithm as with the Power Profile Pilot).

#### Inital Population
Create random values in predefined ranges. We use a population of 10. We start with predefined ranges to make learing much faster.
With these ranges chances are quite good that the car will move right from the beginning. But it's still possible
that with some configuration (an genom/item of the population) the car wont move.

#### Evaluation

The evaluation phase is done by driving one lap on the real race track. The algorithm does the time tracking on its own.
It doesn't rely on round time published by the race track. This speeds up the evaluation as we can start the evaluation
on every location on the track.

- If the car is driving too slow or not at all the evaluation gets aborted.
- A sanity check calculates the expected values for every segment on the track. If there are too many zeros the evaluation gets aborted before it even starts. This speeds up evaluation.
- If the car get a penalty the evaluation gets aborted.

#### Fitness

The measured time in milliseconds acts as fitness score.

#### Pairing and mutation

The population gets resampled by pairing some random items and by randomly mutate some values. The pairing is not fully
random because successful/faster configurations get a higher weight.


#### Iterations

You can trains as long as you like. The state of the learner is persistent. You can switch the algorithms or even restart the server.

After every iteration the configuration/genom of the best score gets written into a file called `data/tot/best_score_{score}.json`. The file
with the best score will be used for the `Train on Track (Auto Pilot)`.

#### Reset

The algorithm will create a directory `data/tot`.

The current population is stored in the file `population.json`.

Delete this file to reset the genetic algorithm. It might be a good idea to create a backup copy of the file to be able to
skip the learning process.

You don't need to restart the server but you'll need to switch to different algorithm (e.g. Manual) and the back to the learner to restart the training.

Learning will take about 30 minutes from 0 to a reasonably good result.



# Appendix

## Degrees of Freedom (Sensor)

**What is Degrees Of Freedom, 6DOF, 9DOF, 10DOF, 11DOF?**

"Degrees Of Freedom" or "DOF" is a number of axis and sensors combined for balancing a plane, a helicopter or a robot.

- 3DOF : This could be a 3-axis accelerometer or it could be a 3-axis gyroscope.
- 6DOF : This is mostly a 3-axis accelerometer combined with a 3-axis gyroscope. <br/> Examples:
    - To control a remote control (RC) plane or helicopter or a self-balancing robot, both the information of the accelerometer and gyro are needed.
    - An other example of 6DOF is a combination of an accelerometer and a magnetometer for a tilt-compensated compass.
    - Many game controllers, phones and tablets contain a 6DOF sensor for motion information.
- 9DOF : This is mostly a 6DOF, combined with a magnetometer (compass).
- 10DOF : This could be a 9DOF, combined with a baromic pressure sensor. The baromic (or absolute pressure) sensor can be used as an indication for the height.
- 11DOF : This could be the 10DOF, combined with a GPS module.

## Genetic Algorithm

![](ga.gif)

