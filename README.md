# EV3 Line Tracer

There are three classes in package `space.oaakx`:

- `EV3AmbientLineTracer` - uses ambient light sensor
- `EV3RedLineTracer` - uses red light sensor
- `EV3RGBLineTracer` - uses RBG color sensor

The best results are achieved with `EV3RGBLineTracer`.

You will need (1) two motors and (2) one color sensor attached to your EV3 brick. Here is how I built my robot:

![My EV3 line following robot](https://i.giphy.com/media/d3mlE7uhX8KFgEmY/source.gif)

## Thought process

I already knew you can detect discrete colors using color sensor. But I was not sure if that's all there is to it. So I started researching and after quick googling I found the 4 modes available for EV3 color sensor: Color, Ambient, Red, RGB.

Then I wanted to see how fast others are able to make their line tracers. After a quick search on Youtube I realized others' line tracers are much faster than what I had in mind. I was going to make a tracer that keeps checking for black and white and keeps adjusting its direction.

### Color mode

Initially, I had the following idea: keep going forward until you detect white, then look sideways for black and after you find black start going forward again. But this was slow. Very slow.

### Ambient light mode

I noticed that black reflects less light while white reflects more and somewhere in-between light reflection is intermediate level. So instead of waiting for white I can correct direction earlier. But still this is slow.

So I had the idea of following the line between black and white. Whenever it gets too white/bright turn, say, left (depending on which side of the line you are in) and whenever it gets too black/dark turn right. I tested this algorithm and it was fast enough (~2:30), but the ambient light sensor precision was too low (value read by it was in range [0.03, 0.07] and only changed by 0.01 at a time).

### Red light mode

Then I discovered tools menu of LeJOS and started playing with values read by color sensor. It wasn't long before I realized red light mode provided more precision for level of black/white. So I replaced the ambient light mode to red light mode. It was more precise and data read ranged between 0.15 and 0.55. It was great for allowing slightly incorrect directions (robot won't loose time constantly adjusting itself). The only problem was there was no way to differentiate white and red (when the robot should stop) so I needed to find some other solution.

### RGB mode

Finally I decided to use RGB mode. It helped to differentiate between white and red.

### Final algorithm

My final algorithm is as follows:

- Keep going between white and black until you encounter red
- Depending on RGB value change individual motor speeds
- Let `f` be function that takes RGB triple and returns single float that represents how much white or black the sensor is on (ranging between `lower` (0.030) and `upper` (0.090)).
- Ideal position to keep the robot is `middle = (upper + lower)/2` (plus minus some error, 0.005 in my case)
- Vary a motor speed depending on `f(rgb)` value
- Say, for left motor, vary speed between 0 and `maxspeed` if `f` is between `lower` and `middle`, and keep it at `maxspeed` between `middle` and `upper`
- For right motor, do the similar: keep the speed at `maxspeed` if `f` is between `lower` and `middle`, and vary speed between `maxspeed` and 0 between `middle` and `upper`
- I defined `f` as level of red in `rgb`. 0.030 meant lots of black and 0.090 meant lots of white in my case.

### Not perfect

This algorithm is not perfect and there are lines that it cannot follow as expected. For example, lines that cross itself will cause robot to skip some parts of the line. But if there is no self-crossings it works fine.
