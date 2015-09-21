# Contribution Guidelines

Follow the official guidelines for [contributing to Open Source on GitHub](https://guides.github.com/activities/contributing-to-open-source/) paying special attention to the following basic principles:

  * Use **bugfix** and **feature** branches to contribute with your edits attending to its nature.
  * **Pull requests** must be against develop branch, never master.
  * Attending to code style, please follow the [official Android guidelines](http://source.android.com/source/code-style.htm) as well as the following tips:

    * **Java** naming conventions:
      * Non-final fields start with a lower case letter
        * `int packagePrivateVar;`
        * `private int privateVar;`
        * `protected int protectedVar;`
        * `private static MyClass singleton;`
        * `public int publicField;`
      * Public static final fields (constants) are `ALL_CAPS_WITH_UNDERSCORES`:
        * `public static final int SOME_CONSTANT = 42;`
      * Class names start with an uppercase letter, and only one
        * `JsonUtils`
      * Interfaces defining contracts: do not add "Interface" suffix
        * `LoginInteractor`
      * Actual Implementation of interfaces: same as interface + "Impl"
        * `LoginInteractorImpl`
      * Special Implementation (in case of several implementations / fake implementations)
        * `FakeLoginInteractorImpl`
        * `LocalLoginInteractorImpl`
    * **Java Android** naming conventions:
      * Activities end with the word "Activity": `LoginActivity`
      * Fragments end with the word "Fragment": `NavigationDrawerFragment`
      * Adapters end with the word "Adapter": `ListAdapter`
    * **Java** Android method params order:
      * Leave the Context param as the last parameter. Only pass it if completely needed! It should be injected most of the times.
    * **Java** fields scope:
      * Pay special attention to fields scope (http://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html) and the use of `final` modifier.
    * **Java** code arrangement:
      * Follow Android Studio default arrangement: Preferences > Editor > Code Style > Java > Default.
    * **Java** annotations:
      * We will use the annotations listed here: http://tools.android.com/tech-docs/support-annotations (mainly Nullness and Resource Type).
      * We will use Intellij annotations (as most of our code is decoupled from Android), as explained at the bottom of that article.
    * **Java** *switch* vs *if*:
      * Use *switch* when there are more than 2 cases. Use *if* otherwise.
    * **Java** *enum* vs *public static final* fields:
      * Use *enum* for clarity and easy management
    * **Java** boilerplate:
      * Avoid writing Getters and Setters for direct access to fields. Use only if necessary.
    * **Java Logs** for debugging:
      * Use this convention for the tag: "class:methodName" i.e: `Log.w("NavigationDrawerFragment:onAttach", "Activity must implement   NavigationDrawerInteractionListener.");`
    * **Java Comments**:
      * Comment every non self-explanatory public method. Use JavaDoc format.
    * **Resources** naming conventions:
      * Always use lowercase letters
      * Words used for resources should be separated by "_": `ic_launcher.png`
      * Activity layouts start with the word "activity": `activity_main.xml`
      * Fragment layouts start with the word "fragment": `fragment_navigation_drawer.xml`
      * items corresponding to an adapter should use the following notation: `<name>_item` (e.g. `book_list_item.xml`)
      * ids should use the following notation: `<parent_layout>_<name>_<type>` (e.g. `activity_main_username_textview`)
