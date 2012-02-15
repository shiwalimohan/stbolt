use GraphViz;

$g = GraphViz->new(node=>{shape=>'record'});

#use $wmehash as main data container, with the following structure:
#$wmehash{$wme} = { wmeval=>$wme, $attr=>@values}
#e.g.
#$wmehash{S1} = { wmeval=>S1, D2=>('value1','value2','value3') }
%wmehash = ();

#S1 or the like
$rootwme = "";

my ($hasopen, $hasclosed);
my $rest = "";

$DUMPFILE = $ARGV[0];
$bPrintWmeNum = $ARGV[1];
$sLanguage = $ARGV[2];
$sOutFile = $ARGV[3];

open(DUMPFILE) or die ("Cannot open DUMP file...! $DUMPFILE\n");

$DEBUGFILE = ">" . $DUMPFILE . ".debug";
open(DEBUGFILE) or die ("Cannot open DEBUG file...! $DEGUGFILE\n");

#$GVIZFILE = ">" . $DUMPFILE . ".dot";
#open(GVIZFILE) or die ("Cannot open DOT file...!\n");

$ERRFILE = ">" . $DUMPFILE . ".err";
open(ERRFILE) or die ("Cannot open ERROR file...!\n");

 	$inline = <DUMPFILE>;
    $line = "";
    $hasopen = ($inline =~ /^\s*\(/);
    $hasclosed = ($inline =~ /\)\s*$/);

	#read entire space between parentheses
    while ($hasopen && !($hasclosed))
    {
		chomp $inline;
		$line .= $inline;
		$inline = <DUMPFILE>;
		$hasclosed = ($inline =~ /\)\s*$/);
    }
    $line .= $inline;
   # print "xx=$line";

   #separate wme and everything else[(<wme> the rest)]
    ($wme, $rest) = split " ", $line, 2;

    $rest =~ s/\^predicate//g;
    $rest =~ s/\|//g;
    $rest =~ s/\)\)/\)/g;
    $rest =~ s/\ \ +/\ /g;
    $rest =~ s/^\ //g;
	$reststr = sprintf("%s", $rest);
	$reststr =~ s/\ /\n/g;

#    print DEBUGFILE "rest=$reststr";
     $g->add_node('nodex',label=>$reststr, shape=>'plaintext');


print GVIZFILE $g->as_text;
$g->as_text("$sOutFile.dot");

$g->as_jpeg("$sOutFile.jpg");

close(ERRFILE);
close(DUMPFILE);
close(GVIZFILE);
close(DEBUGFILE);

