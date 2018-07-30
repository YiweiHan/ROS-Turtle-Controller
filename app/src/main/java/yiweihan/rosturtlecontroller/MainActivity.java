package yiweihan.rosturtlecontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.ros.android.RosActivity;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageFactory;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;

import java.io.IOException;

public class MainActivity extends RosActivity implements NodeMain, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "ROSTC";
    private static final double LINEAR_ABS_MAX_WAFFLE = 0.26;
    private static final double ANGULAR_ABS_MAX_WAFFLE = 1.82;
    private static final double LINEAR_ABS_MAX_BURGER = 0.22;
    private static final double ANGULAR_ABS_MAX_BURGER = 2.84;
    private static final double LINEAR_ABS_MAX_TURTLESIM = 3;
    private static final double ANGULAR_ABS_MAX_TURTLESIM = 3;
    private static final String TURTLEBOT_TOPIC = "/cmd_vel";
    private static final String TURTLESIM_TOPIC = "/turtle1/cmd_vel";
    private Switch publishSwitch;
    private double linear = 0;
    private double angular = 0;
    private double linearAbsMax = LINEAR_ABS_MAX_TURTLESIM;
    private double angularAbsMax = ANGULAR_ABS_MAX_TURTLESIM;
    private TextView currentTwistTextView;
    private SeekBar linearSeekBar;
    private SeekBar angularSeekBar;
    private TextView absoluteLimitsTextView;

    public void resetTwist(View view) {
        linear = 0;
        angular = 0;
        linearSeekBar.setProgress(50);
        angularSeekBar.setProgress(50);
        currentTwistTextView.setText(getString(R.string.current_twist_text, linear, angular));
    }

    public void selectModel(View view) {
        if (((RadioButton)view).isChecked()) {
            switch(view.getId()) {
                case R.id.waffleRadioButton:
                    linearAbsMax = LINEAR_ABS_MAX_WAFFLE;
                    angularAbsMax = ANGULAR_ABS_MAX_WAFFLE;
                    break;
                case R.id.burgerRadioButton:
                    linearAbsMax = LINEAR_ABS_MAX_BURGER;
                    angularAbsMax = ANGULAR_ABS_MAX_BURGER;
                    break;
                case R.id.turtlesimRadioButton:
                    linearAbsMax = LINEAR_ABS_MAX_TURTLESIM;
                    angularAbsMax = ANGULAR_ABS_MAX_TURTLESIM;
                    break;
                default:
                    break;
            }
            resetTwist(null);
            absoluteLimitsTextView.setText(getString(R.string.absolute_limits_text, linearAbsMax, angularAbsMax));
        }
    }

    private void initUI() {
        publishSwitch = findViewById(R.id.publishSwitch);
        currentTwistTextView = findViewById(R.id.currentTwistTextView);
        linearSeekBar = findViewById(R.id.linearSeekBar);
        angularSeekBar = findViewById(R.id.angularSeekBar);
        linearSeekBar.setOnSeekBarChangeListener(this);
        angularSeekBar.setOnSeekBarChangeListener(this);
        absoluteLimitsTextView = findViewById(R.id.absoluteLimitsTextView);
        String topics = TURTLEBOT_TOPIC + ", " + TURTLESIM_TOPIC;
        ((TextView)findViewById(R.id.topicTextView)).setText(getString(R.string.topic_text, topics));
        currentTwistTextView.setText(getString(R.string.current_twist_text, linear, angular));
        absoluteLimitsTextView.setText(getString(R.string.absolute_limits_text, linearAbsMax, angularAbsMax));
    }

    public MainActivity() {
        super("ROS Turtle Controller", "ROS Turtle Controller");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initUI();
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            nodeMainExecutor.execute(this, nodeConfiguration);
        } catch (IOException e) {
            Log.e(TAG, "socket error trying to get networking information from the master uri");
        }
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("android/ROSTurtleController");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Publisher<geometry_msgs.Twist> turtlesimTwistPublisher = connectedNode.newPublisher(TURTLESIM_TOPIC, geometry_msgs.Twist._TYPE);
        final Publisher<geometry_msgs.Twist> turtlebotTwistPublisher = connectedNode.newPublisher(TURTLEBOT_TOPIC, geometry_msgs.Twist._TYPE);

        final CancellableLoop loop = new CancellableLoop() {
            @Override
            protected void loop() throws InterruptedException {
                if (publishSwitch.isChecked()) {
                    NodeConfiguration nodeConfiguration = NodeConfiguration.newPrivate();
                    MessageFactory messageFactory = nodeConfiguration.getTopicMessageFactory();
                    geometry_msgs.Vector3 linear = messageFactory.newFromType(geometry_msgs.Vector3._TYPE);
                    geometry_msgs.Vector3 angular = messageFactory.newFromType(geometry_msgs.Vector3._TYPE);
                    linear.setX(MainActivity.this.linear);
                    linear.setY(0);
                    linear.setZ(0);
                    angular.setX(0);
                    angular.setY(0);
                    angular.setZ(MainActivity.this.angular);
                    geometry_msgs.Twist msg = messageFactory.newFromType(geometry_msgs.Twist._TYPE);
                    msg.setAngular(angular);
                    msg.setLinear(linear);
                    turtlesimTwistPublisher.publish(msg);
                    turtlebotTwistPublisher.publish(msg);
                }
                Thread.sleep(100);
            }
        };
        connectedNode.executeCancellableLoop(loop);
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch(seekBar.getId()) {
            case R.id.linearSeekBar:
                linear = i * (2 * linearAbsMax) / 100 - linearAbsMax;
                break;
            case R.id.angularSeekBar:
                angular = -(i * (2 * angularAbsMax) / 100 - angularAbsMax);
                break;
            default:
                break;
        }
        currentTwistTextView.setText(getString(R.string.current_twist_text, linear, angular));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
