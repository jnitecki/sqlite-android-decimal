# Android SQLite with Decimal support
Fork of https://github.com/requery/sqlite-android/ with https://sqlite.org/floatingpoint.html#the_decimal_c_extension statically added. Additionally dynamic loading extensions disabled and metadata and rounding functions enabled. Native libraries are aligned to 16k boundary for compatibility with new Android requirements.

# Thanks
**Requery/SQLite-Android** for base version - my changes are just tiny.

# Checking Decimal functions presence
## Using options
Note: Getting options itself may not be enabled in your SQLite, so trying to inquire options you may get error that function does not exist. This build has getting SQLite build options always enabled, so lack of 'option' functions, means there is no decimal support either.
1. Check option directly sqlite_compileoption_used('ENABLE_DECIMAL') - should return 1 if decimal is enabled
2. Enumerate options using sqlite_compileoption_get - i.e. select sqlite_compileoption_get(option_number) - start from 0 and increment options until you get NULL. If decimal is supported one of the options will be ENABLED_DECIMAL
## Method check
1. Check any of the SQL functions added by extension, e.g. decimal_add

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
    - new option added for identifying existence of decimal extensions - ENABLE_DECIMAL
    - alinging native libraries to 16k boundary
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
    gradlew regeneratePatches

# Synchronize
## Bring submodule to most recent version
    cd sqlite-android
    git reset --hard
    git clean -fd
    cd ..
    git submodule update --init --remote --recursive
## Initial 
    git fetch
    git submodule init
    git submodule update

# Build 
## Build options
If provided in environment variables prefix them with: ORG_GRADLE_PROJECT_
### General
- *VERSION_NAME* - if not present the version defined by requery/sqlite-android/ will be used, otherwise the version specified here will be build. It may contain -SNAPSHOT.
- *PUBLISH_TYPE* - Snapshot or Final - Snapshot adds SNAPSHOT suffix and uploads to Maven Central Snapshot repository, Final ensures that no suffix is added and uploads it to Maven Central Repository for manual publishing. If not provided and *VERSION_NAME* was not present then Snapshot is build, otherwise version type is deducted from provided *VERSION_NAME*
### Maven Central Credenials
- *mavenCentralUsername* - token username from Maven Central Portal - https://central.sonatype.com/usertoken
- *mavenCentralPassword* - token password from Maven Central Portal
### Signing Key Details
1. Key in file on file system
  - *signing.keyId* - last 8 hexadecimal characters of PGP key ID
  - *signing.password* - PGP key password
  - *signing.secretKeyRingFile* - PGP key ring file path
2. Key as ASCII - in gradle.properties or environment variable
  - *signingInMemoryKeyId* - last 8 hexadecimal characters of PGP key ID
  - *signingInMemoryKey* - PGP ASCII Key
  - *signingInMemoryKeyPassword* - PGP key password

## Build commands
    gradlew bundleReleaseAar
    gradlew publish <build_options>
    gradlew publishToMavenCentral <build_options>
    gradlew publishToMavenLocal <build_options>