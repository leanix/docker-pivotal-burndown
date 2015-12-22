# Burndown charts for Pivotal Tracker projects

This app allows to create burndown charts for iterations in [Pivotal Tracker](http://www.pivotaltracker.com/).

The app can create charts in two ways. One way is to create a chart image, 
the second way is pushing the data to a Geckoboard Highcharts widget.

This is a maven application. This means that you have to run "package" once to create the needed files for execution. 
In order to create these files run: 

    mvn clean package 

In order to run the app you need to provide the neccessary credentials via system property parameters when starting the script.

## Available Parameters:

### pivotal.projectId
This is the ID of the Pivotal Tracker project to get the burndown chart for. 

#### pivotal.apiKey
The API-Key to access Pivotal Tracker via the REST-API

#### geckoboard.apiKey (optional)
The API-Key to access your Geckoboard

#### geckoboard.widgetKey (optional)
The Key of the widget which should display the data.

#### imageTargetPath (optional)
The Path where the burndown chart image should be stored.

#### iteration (optional)
The iteration number in Pivotal Tracker. If left empty the current iteration is taken.

## Usage

### Push to Geckoboard
If you want to publish the burndown chart to a Geckoboard widget. You need to provide 
both the geckoboard.apiKey and the geckoboard.widgetKey parameters. The widget must be set to Highcharts mode. 
The width and height settings can be set as you like.

You only need to execute the app with the parameters as below:

    mvn exec:java -Dpivotal.apiKey=1234 -Dpivotal.projectId=0815 -Dgeckoboard.apiKey=125 -Dgeckoboard.widgetKey=123
    
### Create Burndown Graph Image
In this case you only need to provide the target path to the image. Within this path a file named burndown_{iteration}.png 
will be created. If this file already exists in this folder it will be overridden, if writable.

The command in this case looks like this:

    mvn exec:java -Dpivotal.apiKey=1234 -Dpivotal.projectId=0815 -DimageTargetPath=PATH_TO_STORE
    
    
### I want both
If you want both you just need to add both parameters to the exec command.

### I want a specific Iteration Burndown
If you want the burndown chart for a specific iteration you just need to add "-Diteration=123" to the exec command 
replacing 123 with the desired iteration number.


## Links:

Pivotal Tracker: https://www.pivotaltracker.com
Geckoboard: https://www.geckoboard.com
Highcharts: http://www.highcharts.com/
