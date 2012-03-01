#!/usr/bin/perl

$singlePriority = -1;
if ($#ARGV >= 0) {
	$singlePriority = shift;
}

# sentences structure:
# sentence, correct (sorted) DRS, priority
# priority 1: must handle for demo 
# priority 2: alternate phrasings

@sentences = (
	## noun sentences
	# derived from the 1/27/12 ppt emailed from Craig Schlenoff
	# This is an <adj?> <noun>.
	["This is a red block.",
		"this(N2) red(N2) DEF(N2) is(N2,ball)",
		1],
	["This is a block.", 
		"this(N2) DEF(N2) is(N2,block)",
		1],
	["This is a ball.", 
		"this(N2) DEF(N2) is(N2,ball)",
		1],
	["That is a block.", 
		"that(N2) DEF(N2) is(N2,block)",
		2],
	["This is a yellow block.", 
		"this(N2) yellow(N2) DEF(N2) is(N2,block)",
		1],
	["That is a red ball.", 
		"red(N2) DEF(N2) is(N2,red)",
		2],
	# These are <adj?> <noun>.
	["These are blue blocks.", 
		"these(N2) blue(N2) are(N2,blocks)",
		1],	
	["These are blocks.", 
		"these(N2) are(N2,blocks)",
		1],
	["Those are red balls.", 
		"red(N2) those(N2) are(N2,balls)",
		2],
	# The <adj?> <noun> is <spatial relation> the <adj?> <noun>.
	["The red ball is left of the blue block.",
		"left-of(N3,N6) is(N3,ball) DEF(N3) blue(N6) DEF(N6) red(N3) is(N6,block)",
		1],
	["The red ball is left of that blue block.",
		"left-of(N3,N6) is(N3,ball) DEF(N3) blue(N6) DEF(N6) red(N3) is(N6,block) that(N6)",
		2],
	["The block is right of the ball.",
		"is(N2,block) DEF(N2) DEF(N4) is(N4,ball) above(N2,N4)",
		1],
	["The ball is on the table.",
		"on(N2,N4) table(N4) DEF(N2) is(N2,ball) DEF(N4)",
		1],
	["The red block is on the blue block.",
		"on(N3,N6) is(N3,block) DEF(N3) blue(N6) DEF(N6) red(N3) is(N6,block)",
		1],
	["The yellow ball is behind the white ball.",
		"is(N3,ball) DEF(N3) DEF(N6) behind(N3,N6) white(N6) is(N6,ball) yellow(N3)",
		1],
	["That purple block is in front of the ball.",
		"is(N3,block) in-front-of(N3,N5) DEF(N5) that(N3) DEF(N3) purple(N3) is(N5,ball))",
		2],
	# Which is the <adj?> <noun>?
	["Which is the yellow block?",
		"",
		1],	
	["Which is the block?",
		"",
		1],	
	["Which is a block?",
		"",
		2],	
	["Which is a green block?",
		"",
		2],	
	# Describe this object? (with pointing)
	["Describe this object.",
		"",
		1],	
	["Describe this.",
		"",
		2],	
	["Describe this block.",
		"",
		2],	
	["Describe that object.",
		"",
		2],	
	["Describe the object.",
		"",
		2],	
	["Describe the ball.",
		"",
		2],	
	# What is this? (with pointing)
	["What is this?",
		"",
		1],	
	["What is that?",
		"",
		2],	
	# What is the color of this? (with pointing)
	# What is the size of this? (with pointing)
	# What is the shape of this? (with pointing)
	["What is the color of this?",
		"",
		1],	
	["What color is this?",
		"",
		2],	
	["What is the color of that?",
		"",
		2],	
	["What color is that?",
		"",
		2],	
	["What is the size of this?",
		"",
		1],	
	["What is the shape of this?",
		"",
		1],	
	# Describe the spatial relationship between <adj?> <noun> and <adj?> <noun>.
	["Describe the spatial relationship between the red ball and the yellow block.",
		"",
		1],	
	["Describe the spatial relationship between the ball and the yellow block.",
		"",
		1],	

	["Describe the relationship between the black block and the ball.",
		"",
		2],	

	## verb sentences
	["Pick up the ball.",
		"",
		1],	
	["Pick up the red ball.",
		"",
		1],	
	["Put the block on the table.",
		"",
		1],	
	["Put the block on top of the table.",
		"",
		2],	
	["Now put the block on the table.",
		"",
		2],	

	["You are done.",
		"",
		1],	
	["That is all.",
		"",
		2],	
	["The action is complete.",
		"",
		2],	
);

$minPriority = 100;
$maxPriority = 0;
for ($i=0; $i<=$#sentences; $i++) {
	if ($sentences[$i][2] < $minPriority) {
		$minPriority = $sentences[$i][2];
	}
	if ($sentences[$i][2] > $maxPriority) {
		$maxPriority = $sentences[$i][2];
	}
}

PRIORITY:
for ($p=$minPriority; $p <= $maxPriority; $p++) {
	if ($singlePriority > -1 and $p != $singlePriority) {
		next PRIORITY;
	}
	print "******** BEGIN PRIORITY $p SENTENCES ************\n";
	SENTENCE:
	for ($i=0; $i<=$#sentences; $i++) {
		if ($sentences[$i][2] != $p) {
			next SENTENCE;
		}
		$sentence = $sentences[$i][0];
		$correctDRS = $sentences[$i][1];

		print "test sentence: $sentence\n";
		@output = `./run.sh "$sentence" --silent`;

		$outDRS = `echo "@output" | ./normalizeDRS.pl`;;
		
		if ($outDRS eq $correctDRS) {
			print "PASSED: $correctDRS\n";
		}
		else {
			print "FAILED:\n";	
			print "\texpected $correctDRS\n";
			print "\tgot $outDRS\n";
			print "Full LGSoar output:\n";
			print @output;
		}
		print "********************\n";
	}
}
