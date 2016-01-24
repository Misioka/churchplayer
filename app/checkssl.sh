#!/bin/bash
sslworkdir="ssl_work_dir"
if [ ! -d $sslworkdir ]; then
  mkdir $sslworkdir
fi
unzip -q "$1" -d $sslworkdir
#Set delimiter to ignore spaces
IFS=$'\r\n'
#Create an array of OpenSSL version strings
opensslarr=($(egrep --binary-files=text -o -R -e "OpenSSL\s\d+\.\d+\.\d+\w+\s\d+\s\w+\s\d+" $sslworkdir/*))
#Stackoverflow syntax highlight fix closing 'block comment' */
if [ ${#opensslarr[@]} -gt 0 ]; then
    echo "Found OpenSSL versions"
    printf "%s\n" "${opensslarr[@]}"
    heartbeatarr=($(grep -R -E "(tls1_process_heartbeat|dtls1_process_heartbeat|dtls1_heartbeat|tls1_hearbeat)" $sslworkdir/*))
    #Stackoverflow syntax highlight fix closing 'block comment' */
    if [ ${#heartbeatarr[@]} -gt 0 ]; then
        echo "Files that contains heartbeat methods:"
    printf "%s\n" "${heartbeatarr[@]}"
    else
        echo "No libraries contain heartbeat methods"
    fi
else
    echo "Did not find OpenSSL"
fi
rm -rf $sslworkdir
