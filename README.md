Introduction
------------

This Java server is a REST server to use in conjunction with the Jedi PowerDNS backend
server that I wrote.  This was developed as an exercise in developing REST-based 
services using JAX-RS, Jersey, and Jetty, and to play with Amazon's Dynamo a little bit.
It could be extended to handle more types of records in the future, and could potentially
be used as a backend for an authoritative DNS service.  Everything herein is developed 
based on open-source software, and the end user is responsible for complying with any
restrictions on the use thereof.

It can be configured to use Amazon Dynamo or a local file-based LevelDB 
database for record storage.  It can currently store and reply with A and AAAA records, 
but could easily be extended in the future to handle other types as well.   Requests
are digest authenticated if authentication is configured.  The server is based on
Jetty 8.  No magic was used in the creation of this application, everything should be
straightforward and easily understood.

...Except for the use of the JAX-RS extensions to Java.  I think those are magic, but
it couldn't be helped.  And while we're at it, the apache httpcomponents library isn't
the most transparent or easy to use thing in the world, which is probably why it 
undergoes such massive changes between versions.  Unfortunately, not much else out there.

Everything herein is developed based on open-source software, and the end user
is responsible for complying with any restrictions on the use thereof.  If you
are interested in having a custom backend written for you or a modification
done, I may be available for contract work.


Building and Installation
-------------------------

The server build requires access to the statsd jar built from [this github repo] [sd].
The jar can be built and installed into your local maven repo (usually ~/.m2) via
"mvn package install".  This is the only dependency that is not available on public
maven repository sites.

The server can be built as an executable jar with a simple "mvn package" command.
The output jar file will be in the target/ directory.

  [sd]: http://www.github.com/dhawth/java_statsd

From scratch on an ubuntu box:

apt-get install maven git gcc openjdk-7-jdk openjdk-7-jre

Note that you may need to run update-alternatives --config java to change your JVM to 7
if you have both installed on your system.

build statsd:

git clone https://github.com/dhawth/java_statsd.git
cd java_statsd
mvn clean package install

Build darkside:  this requires a localhost dynamo instance for tests.  It can
be downloaded via this link, and there are instructions on running it.  Basically you
just untar it, then java -jar DynamoDBLocal.jar.  You may need to set your
LD_LIBRARY_PATH to the path of the untarred directory.

http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html

Back to building darkside:

mvn clean package

ls target/darkside-1.0.0-jar-with-dependencies.jar

Runtime Configuration
---------------------

There is an example configuration file in examples/ that uses the same mock dynamo
localhost instance for a backend.

The server requires a configuration file as well as a log4j properties file.  These
are passed in with the -c and -l flags, respectively.  A sample log4j.conf file is
provided in src/test/resources/log4j.conf.  A sample server configuration file is also
available in src/test/resources/test.conf, although you will obviously want to
modify it.  The configuration for this server is in json, and options, default values,
and explanations for the config are available in the DarksideConfig.java class.

This server supports statsd data collection internally via the java_statsd library,
which improves performance by collecting statistics in a singleton StatsObject and then
shipping them once a second with the configured statsd_client_type.  If no statsd_client_type
is specified, stats are simply discarded once a minute.  It is recommended to use
the client type "udp" if you are using stock statsd implementations provided by others.

Default TTLs for responses can be configured, in seconds, and will be returned with
the answers for the records in question when the records themselves do not already have
TTLs when they are retrieved from the backing database.

Obviously you'll need to create your table in Dynamo if you're using Dynamo.

Runtime Testing / Operational Aspects
-------------------------------------

Obviously a graphite server is a great idea, and more statsd counters and timers could be
added to the codebase to cover any overlooked aspects of the runtime.

Testing responsiveness can be done directly by issuing HTTP requests.  Examples of request 
and response formats and return codes are available in the test classes.  All requests/responses 
are in json.

All request paths look like this: /fqdn/1/$hostname, e.g. http://darkside.test.com/fqdn/1/foo.bar.baz
For fetching records from the database, use HTTP GET.  For removing records, HTTP DELETE, and
for updating/creating records, HTTP POST.  All work is done by the RestHandler class.

Very little verification of content is done on insert.  It is expected that the interfaces
exposed by this server are used within an existing infrastructure and are not exposed to
end-users, and therefore a certain level of reliability of input can be assumed.

Extension
---------

If for some reason other attributes need to be stored with records, you will 
want to modify the DNSRecord class accordingly.  You will then probably want to add more verification code to POSTs in the RestHandler class.

Dynamo Integration
------------------

There is a second executable class in the jarball called CreateDynamoTable.
It can be executed via

java -cp target/darkside-1.0.0-jar-with-dependencies.jar org.devnull.darkside.CreateDynamoTable -c examples/dynamo.conf

The dynamo.conf configuration file needs to have the accesskey, secret key, 
name of the table you want to create, and the endpoint URL to connect to.  
This program can be used with mock dynamo instances to set them up in testing 
environments, or for production.

An example config file that works against a localhost mock Dynamo instance to create the fqdn table:

{
	"tableName" : "fqdn",
	"accessKey" : "foo",
	"secretKey" : "bar",
	"endPoint"  : "http://localhost:8000"
}

Records are stored in Dynamo as json strings.  This is because Dynamo has support for top-level
data structures, but no nested data structures.  It was therefore not possible to simply save
a DNSRecord with its associated list of IPRecord objects, and was necessary to serialize the
DNSRecord into a json string.
