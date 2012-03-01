#!/usr/bin/perl

@DRS = ();
foreach $line (<>) {
	chomp $line;
	if ($line =~/\(.*\)/) {
		push @DRS, $line;
	}
}

@DRS = sort @DRS;

for ($i=0;$i<$#DRS;$i++) {
	print "$DRS[$i] ";
}
print "$DRS[$#DRS]\n";
