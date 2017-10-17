# NeuralNetworkAPI
A bukkit plugin for adding neural networks into minecraft.

##  How to use (Basic-Demo)
* To start, use `/nn cnn` or `/nn createNewNN` to see the list of all neural network types. I would recommend selecting `LogicalOR` to start off with
* Then, use `/nn startlearning` to start training the NN.
* Almost immediately after sending that command, use `/nn stoplearning` to stop training. Within the seconds of sending these two commands, the bot has gone through over a thousand scenarios, and should be 100% accurate. (For other NNs, you can check the accuracy by checking the console.)
* Now, to test it, use `/nn test` to test two inputs, either true or false. (example: `/nn test true false`)
* For LogicalOR, it should print out true if either the first and/or the second input is true.


### For non-developers
This plugin provides nothing on its own besides a few demos of what this API can achieve. If you do not plan on developing your own NeuralNetwork plugin or using another plugin the uses this API, this plugin may be useless to you.

### For plugin developers
There are a few things that you should know before attempting to make a new Neural network type
* The `example` package gives a number of examples of how to create NNs. The logic gate NNs should be the easiest to understand.
* By default, all NNs that extends NNBaseEntity automatically implement ConfigurationSerializable. However, in order for bukkit to be able to load these values from the config, you will need to create a new construcor for the class like so: 
````java
  public CustomNeuralNetwork(Map<String,Object> map){
    super(map);
  }
```` ...and let the ConfigurationSerialization class register the object in the onEnable or onLoad of the main class like so:
````java
  public void onEnable(){
    ConfigurationSerialization.registerClass(CustomNeuralNetwork.class);
  }
```` After this, you should be able to save and load the `NNBaseEntity` to and from the config. Note that you cannot save the `NeuralNetwork` instance to the config.
* Ideally, no other part of your plugin should directly reference your custom neural network object. Instead, all calls to that object should be done through the NeuralNetwork object.
* For best results, only call `DeepReinformentUtil.instantaneousReinforce` when the NN failed in any way. Calling it when it returned the correct value tends to make it "forget" what it may have already learned.
* This API is still in development. Some aspects of this plugin may change in future updates. Continually to check the github page for updates and make sure your project always references the newest version.
* When traing your NeuralNetwork, if you do not want the console to fill up with debug messages, use `NeuralNetwork#setBroadcasting(false);` to disable console debugging.
* If your AI is not return the values you expect, or want to know what your NN is "thinking" given an input, you can open the grapher to have a visualisation of what is happening. use `NeuralNetwork#openGrapher();` to open the grapher instance. Once you are done, you can use `NeuralNetwork#closeGrapher();` to propperly shut it down.


###How this works
NeuralNetworks start with three basic components, a Sensory array, an AI object, and a Controler object.
* The Sensory array determins the inputs. This could be booleans, representing a true or false value, or numbers which represent possible states the for the input.
* The AI is what converts the inputs into outputs. Through a series of neurons, each with their own values and thresholds, the information is passed from the input into outputs based on how the NN has been trained.
* The Controler is how you convert the outputs into data that you can use. For example, the controler can convert the outputs into a boolean, determining if the AI detected a swear word for a swear filter, or determining the sum of two integers.