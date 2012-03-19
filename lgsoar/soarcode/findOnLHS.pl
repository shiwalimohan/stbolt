#!/usr/bin/perl

$string = shift @ARGV;

$state = 0;
foreach $file (@ARGV) {
	LINE:
	foreach $line (`cat $file`) {
		#print "$state** $line";
		$line =~ s/#.*$//;
		if ($line =~ /\{/) {
			if ($line =~ /\}/) {
				next LINE;
			}
			if ($state == 0) {
				$state = 1;
				if ($line =~ /sp.*\{(.*)/) {
					$prod = $1;
				}
				else {
					print "bad open: $file $line";
				}
			}
			elsif ($state == 1) {
				$state = 11;
			}
		}
		elsif ($line =~ /-->/) {
			if ($state != 1) {
				print "ERROR: bad state in $file $prod $line";
			}
			$state = 2;
		}
		elsif ($line =~ /\}/) {
			if ($state == 11) {
				$state = 1;
			}
			elsif ($state != 2) {
				print "ERROR: bad state in $file $prod $line";
			}
			else {
				$state = 0;
			}
		}

		if ($state == 1 || $state == 11) {
			if ($line =~ /$string/) {
				print "file: $file prod: $prod\n";
				print $line;
			}
		}
	}
}
