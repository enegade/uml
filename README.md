
### Java code to UML tool. Java profiler. Java debugger. Java code visualisation tool.

By doing this project we try to create the tool to visualise java executable code into UML-like diagrams. To achieve this goal we use dynamic analyse as opposed to static analyse (such as using JavaCC). The idea is to use java debugger (com.sun.jdi) API to collect information about executable code and produce an output in a text format which later can be visualised by Plantuml capabilities (see https://plantuml.com/). it seems like we've fulfilled the goal and made it possible to visualise code into sequence and class diagrams. Though there are some limitations.

We focused on these objectives:
- Process of collecting debug information must be effective, that is, to reduce elapsing time needed to do debugging process. To do it we debug programs by traversing a single method only one time and in case of a loop we do only one iteration. This decision imposes a restriction on the tool functionality. For example conditional constructions and polymorphic behavior are ignored. But you still can get inside such blocks (by change configuration in config.properties). 
- We need a possibility to debug multithreading programs. For this we build the thread graph to identify threads. You can build diagrams for one selected (in config.properties) thread.
- We need a possibility to debug programs which use console input/output streams. For this we redirect input/output stream data from a debuggee program into same streams of the debugger. We redirect output stream data to be streamed into terminal console. Input stream data can be entered straight into the terminal console.
- We need a possibility to control the visibility area of the diagrams, that is, starting points for a sequence diagram and quantity of visible elements. The class diagram must match the sequence diagram in the selected area. During the first launch the tool collects data about debuggee executable code. Collected data is serialized into a file with name call_graph.data. Subsequent launching of the tool utilize that data without repeating the debugging process.


### Building

You need java version 11. To build the project use maven command:
```code
mvn clean package
```
Find built snapshot in a target directory.


### Before running

Before running executable file do this steps:
- create working directory (where outputs will be located)
- move executable jar into that directory
- copy config file config.properties in that directory
- move previously downloaded plantuml.jar into that directory


### Working example

To gain a general feeling of the tool you can try this example case.

<details>
  <summary><i>click to expand</i></summary>

In this example we try to explain base features. For simplicity, we'll be using junit example. For prerequisites, you need to look at https://github.com/junit-team/junit4/wiki/Getting-started

Do this steps:

- Do [before running](#before-running) steps.

- Edit config.properties and set parameters:
```code
startup.from.class=CalculatorTest
startup.from.method=void evaluatesExpression()
```
Edit <i>argument.options</i> parameter, so it points to a right classpath where junit libraries and example code are located.

- Move into working directory and use terminal to run commands:
```code
java -jar uml-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

- Output files class.txt and sequence.txt represents uml diagrams in plantuml text format. You can convert it to svg format using commands:
```code
java -jar plantuml.jar sequence.txt -tsvg
java -jar plantuml.jar class.txt -tsvg
```

- To acquire diagrams in convenient png format you need to use inkscape tool (installing separately) that can convert svg to png. Run commands:
```code
inkscape -z -e sequence.png -w 3518 -h 3294 -b '#ffffff' sequence.svg
inkscape -z -e class.png -w 2408 -h 1126 -b '#ffffff' class.svg
```
||
|----|
| :point_up: <i>There are tips for using inkscape. To change output image scale use option -w to set required width and -h to set required height. Original scale you can find in inkscape output text (look for text like </i>Area 0:0:2408:1126 exported to <i>).</i> |

<details>
  <summary>resulting diagrams (<i>click to expand</i>)</summary>
 
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/sequence_1.png?raw=true)
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/class_1.png?raw=true)
</details>

You can see thread map of running code. For that run
```code
java -jar plantuml.jar threads.txt -tsvg
inkscape -z -e threads.png -w 729 -h 94 -b '#ffffff' threads.svg
```
<details>
  <summary>resulting thread graph (<i>click to expand</i>)</summary>
  
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/threads.png?raw=true)
</details>

Diagrams can be built for code running in a selected thread. By default, the main thread is selected. It is possible to select other thread by editing thread_creating_order parameter in config.properties.

Output file call_graph.data contains serialized call graph of the running code. In the first run the tool generates this file. In the subsequent runs the tool checks for the presence of this file and if so does not run debuggee program again. This file contains information that is collected during debugger process and used to build diagrams. Area visible on diagrams is limited by a configuration, so you can change displayed scope by editing *_types.json file.

To have ability to change visible area on the diagrams we use selectors that represented in json format. Using the selectors you can specify a type or a method. *_types.json contains a list of all types, information about which was collected during debugging process. The diagrams are built in specified way. The sequence diagram is built first. Then types and relations displayed on the sequence diagram are reflected on the class diagram. You can apply filters to types and methods. Existed filters are: **starting**, **trimming**, **hidden**, **suppressed**, **skipped**. If you apply a filter to a type it means that the filter is applied to all methods defined by the type.
- **starting** - specify methods the sequence diagram is started from.
- **trimming** - specify methods the sequence diagram is stopped on. These methods are rendered on the diagram in a red color. By default, the rendering depth is 5. You can change this behavior by editing config.properties.
- **suppressed** - specify methods the sequence diagram is stopped on. These methods are rendered like simple operation. By default, all methods defined in String type are **suppressed**. If within visible part of the sequence diagram an object is created and this object has not calls from or to, then **suppressed** filter is applied to constructor of this object. The types which is pointed by this filter are not rendered on the class diagram. By default, Object type is not rendered on the class diagram.
- **hidden** - specify methods which are not rendered on the diagrams.
- **skipped** - specify methods which are not rendered on the diagrams. But transitive methods which are called inside will be rendered.

For example, you can edit the diagram from the previous steps:
- Find a type selector named <i>java.lang.ClassLoader</i>.
- Find a method named <i>java.lang.Class loadClass(java.lang.String)</i>.
- Change the **hidden** parameter of this method to <i>true</i>.
- Do the steps to obtain diagrams:
```code
java -jar uml-0.0.1-SNAPSHOT-jar-with-dependencies.jar
java -jar plantuml.jar sequence.txt -tsvg
java -jar plantuml.jar class.txt -tsvg
inkscape -z -e sequence.png -w 1809 -h 1321 -b '#ffffff' sequence.svg
inkscape -z -e class.png -w 759 -h 801 -b '#ffffff' class.svg
```


<details>
  <summary>resulting diagrams (<i>click to expand</i>)</summary>
  
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/sequence_2.png?raw=true)
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/class_2.png?raw=true)
</details>

Besides **loop** constructions you can use **alt** constructions:
- Find a type selector named <i>java.lang.Integer</i>.
- Find a method named <i>int parseInt(java.lang.String, int)</i>.
- Add condition selector to the <i>conditions</i>:
```code
          {
            "conditionType": "ALT",
            "title": "if (len > 0) {",
            "startLine": 632,
            "endLine": 660,
            "components": []
          }
```
- Do the steps to obtain diagrams.

<details>
  <summary>resulting diagrams (<i>click to expand</i>)</summary>
  
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/sequence_3.png?raw=true)
</details>

You can hide several method calls from a particular method by using the **hiding** construction. Place it into <i>hidingConditions</i> selector:
- Find a type selector named <i>CalculatorTest</i>.
- Find a method named <i>void evaluatesExpression()</i>.
- Add **hiding** selector to the <i>hidingConditions</i>:
```code
          {
            "conditionType": "HIDING",
            "title": "",
            "startLine": 7,
            "endLine": 7,
            "components": []
          }
```
- Do the steps to obtain diagrams.

<details>
  <summary>resulting diagrams (<i>click to expand</i>)</summary>
  
  ![alt text](https://github.com/enegade/uml/blob/main/docs/imgs/readme/sequence_4.png?raw=true)
</details>

</details>


I hope you'll find this tool useful for some of your use cases. You can address me any issues. Any contribution are welcomed.

### TODO

- Provide tests and API documentations.
- Implement logging.
