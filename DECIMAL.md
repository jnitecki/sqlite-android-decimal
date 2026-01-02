# Android SQLite with Decimal support (sqlite-android-decimal)
Fork of https://github.com/requery/sqlite-android/ with https://sqlite.org/floatingpoint.html#the_decimal_c_extension statically added, dynamic loading extensions disabled as well as metadata and rounding functions enabled

# Code Modifications
1. sqlite-android\src\main\jni\sqlite\decimal.c from SQLite distibution https://sqlite.org/src/file?ci=trunk&name=ext%2Fmisc%2Fdecimal.c
2. sqlite-android\src\main\jni\sqlite\decimal_init.c
   - custom initalizer for decimal support
3. sqlite-android\src\main\jni\sqlite\Android.mk 
   - added new files to compilation (decimal.c and decimal_init.c)
   - added extra build options
     - disabling dynamic extensions loading (SQLITE_OMIT_LOAD_EXTENSION=0)
     - invoking method from decimal_init.c during initialization of SQLite (SQLITE_EXTRA_INIT=sqlite3_decimal_extension_init)
	 - other options (SQLITE_ENABLE_COLUMN_METADATA & SQLITE_ENABLE_MATH_FUNCTIONS)
4. sqlite-android/src/main/jni/sqlite/android_database_SQLiteConnection.cpp
   - disabled calls for dynamic extension loading - since it is now disabled in Android.mk
5. gradle.properties
   - publishing group updated (to com.tbss-ltd)
   - repo location
6. sqlite-android/gradle.properties
   - pom details updated to match new package details
7. sqlite-android/build.gradle
   - adding invoke to custom patch project
  
# Generate patch
git diff --no-index sqlite-android\src\main\jni\sqlite\Android.mk sqlite-android\patches\Android.mk > sqlite-android\patches\Android.mk.patch

# Synchronize
git fetch upstream
- get last tag from https://github.com/requery/sqlite-android/tags
git checkout master
git pull upstream tags/3.49.0
git checkout -b decimal-update-3.49.0 tags/3.49.0

git push origin
git branch decimal-update




# Build 
gradlew bundleReleaseAar
gradlew publish

