#!/usr/bin/perl
$started = 0;
foreach $line (<>) {
	chomp $line;
	if ($started == 0 and $line =~/message structure/) {
		$started = 1;
	}
	elsif ($started == 1 and $line =~ /\w/) {
		$line =~ s/\(//g;
		$line =~ s/\)//g;
		$line =~ s/\b[A-Z]+[0-9]+\b//g;
		$line =~ s/\^//g;
		$line =~ s/^ //;
		if (not $line =~ /id 0/) {
			print "$line\n";
		}
	}
}

