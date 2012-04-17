#!/usr/bin/perl
$cmd = "java -classpath ./bin:/opt/bolt/soar/share/java:/opt/bolt/soar/share/java/soar-debugger-9.3.1.jar com.soartech.bolt.SoarRunner soarcode/load.soar";

# need to preserve quoted arguments
foreach $arg (@ARGV) {
	if ($arg =~ / /) {
		$cmd .= " \"$arg\"";
	}
	else {
		$cmd .= " $arg";
	}
}

exec($cmd);
