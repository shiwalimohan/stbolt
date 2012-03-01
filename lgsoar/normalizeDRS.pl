#!/usr/bin/perl

@DRS = ();
foreach $line (<>) {
	chomp $line;
	if ($line =~/(\S+\(.*\))/) {
		push @DRS, $1;
	}
}

@DRS = sort @DRS;

for ($i=0;$i<$#DRS;$i++) {
	print "$DRS[$i] ";
}
print "$DRS[$#DRS]\n";
