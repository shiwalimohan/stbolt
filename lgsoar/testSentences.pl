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
		"",
		1],
	["This is a block.", 
		"",
		1],
	["This is a ball.", 
		"",
		1],
	["That is a block.", 
		"",
		2],
	["This is a yellow block.", 
		"",
		1],
	["That is a red ball.", 
		"",
		2],
	# These are <adj?> <noun>.
	["These are blue blocks.", 
		"blocks(N2) blue(N2) these(N2)",
		1],	
	["These are blocks.", 
		"blocks(N2) these(N2)",
		1],
	["Those are yellow blocks.", 
		"blocks(N2) those(N2) yellow(N2)",
		2],
	# The <adj?> <noun> is <spatial relation> the <adj?> <noun>.
	["The red ball is left of the blue block.",
		"",
		1],
	["The red ball is left of that blue block.",
		"",
		2],
	["The block is right of the ball.",
		"",
		1],
	["The ball is on the table.",
		"DEF(N2) DEF(N4) ball(N2) on(N2,N4) table(N4)",
		1],
	["The red block is on the blue block.",
	 "DEF(N3) DEF(N6) block(N3) block(N6) blue(N6) on(N3,N6) red(N3)",
		1],
	["The yellow ball is behind the white ball.",
		"DEF(N3) DEF(N6) ball(N3) ball(N6) behind(N3,N6) white(N6) yellow(N3)",
		1],
	["That purple block is in front of the ball.",
		"",
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
	 "DEF(N3) HEARER(N4) describe(N4,N3) object(N3)",
		1],	
	["Describe this.",
		"HEARER(N4) describe(N4,N3) this(N3)",
		2],	
	["Describe this block.",
		"DEF(N3) HEARER(N4) block(N3) describe(N4,N3)",
		2],	
	["Describe that object.",
		"DEF(N3) HEARER(N4) describe(N4,N3) object(N3)",
		2],	
	["Describe the object.",
		"DEF(N3) HEARER(N4) describe(N4,N3) object(N3)",
		2],	
	["Describe the ball.",
		"DEF(N3) HEARER(N4) ball(N3) describe(N4,N3)",
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
		"DEF(N3) HEARER(N4) ball(N3) pick(N4,N3)",
		1],	
	["Pick up the red ball.",
	 "DEF(N4) HEARER(N5) ball(N4) pick(N5,N4) red(N4)",
		1],	
	["Put the block on the table.",
		"DEF(N3) DEF(N4) HEARER(N5) block(N3) on(put,N4) put(N5,N3) table(N4)",
		1],	
	["Put the block on top of the table.",
		"",
		2],	
	["Now put the block on the table.",
		"",
		2],	

	["You are done.",
		"done(N2) you(N2)",
		1],	
	["You moved the red block to the table.",
		"DEF(N5) DEF(N6) block(N5) moved(N2,N5) red(N5) table(N6) to(moved,N6) you(N2)",
		1],
	["The action is complete.",
		"action(N2) complete(N2)",
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

$passCount = 0;
$failCount = 0;

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
		@output = `./run.sh "$sentence"`;

		$outDRS = `echo "@output" | ./normalizeDRS.pl`;
		chomp $outDRS;
		
		if ($outDRS eq $correctDRS and $outDRS ne "") {
			print "PASSED: $correctDRS\n";
			$passCount++;
		}
		else {
			print "FAILED:\n";	
			print "\texpected [$correctDRS]\n";
			print "\tgot [$outDRS]\n";
#			print "Full LGSoar output:\n";
#			print @output;
			$failCount++;
		}
		print "********************\n";
	}
}

$total = $passCount + $failCount;

print "passed $passCount of $total tests.\n";
