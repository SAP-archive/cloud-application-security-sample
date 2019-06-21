REM This script prepares the current shell's environment variables (not permanently)
REM VCAP_APPLICATION is required when cloud profile is active

SET VCAP_APPLICATION={}
SET SPRING_PROFILES_ACTIVE=cloud,uaamock

