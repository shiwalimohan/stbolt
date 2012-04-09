#!/usr/bin/perl

if ($ARGV[0] == "--simple") {
	$singlePriority = 1;
	$simple = 1;
}
else {
	$singlePriority = -1;
	if ($#ARGV >= 0) {
		$singlePriority = shift;
	}
	$simple = 0;
}

# sentences structure:
# sentence, correct (sorted) DRS, priority

@sentences = (
	## noun sentences
	# derived from the 1/27/12 ppt emailed from Craig Schlenoff
	# This is an <adj?> <noun>.
	["This is a red block.",
		"INDEF(N2) block(N2) red(N2) this(N2)",
		1],
	["This is a block.", 
	 "INDEF(N2) block(N2) this(N2)",
		1],
	["This is a ball.", 
	 "INDEF(N2) ball(N2) this(N2)",
		1],
	["That is a block.", 
		"INDEF(N2) block(N2) that(N2)",
		1],
	["This is a yellow block.", 
		"INDEF(N2) block(N2) this(N2) yellow(N2)",
		1],
	["That is a red ball.", 
		"INDEF(N2) ball(N2) red(N2) that(N2)",
		1],
	# These are <adj?> <noun>.
	["These are blue blocks.", 
		"blocks(N2) blue(N2) these(N2)",
		1],	
	["These are blocks.", 
		"blocks(N2) these(N2)",
		1],
	["Those are yellow blocks.", 
		"blocks(N2) those(N2) yellow(N2)",
		1],
	# The <adj?> <noun> is <spatial relation> the <adj?> <noun>.
	["The red ball is left of the blue block.",
		"",
		2],
	["The red ball is left of that blue block.",
		"",
		2],
	["The red ball is to the left of the blue block.",
		"DEF(N3) DEF(N5) DEF(N7) ball(N3) block(N7) blue(N7) left(N5) of(N5,N7) red(N3) to(N3,N5)",,
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
		1],
	# Which is the <adj?> <noun>?
	["Which is the yellow block?",
	  "DEF(N2) block(N2) which(N2) yellow(N2)",
		1],	
	["Which is the block?",
		"DEF(N2) block(N2) which(N2)",
		1],	
	["Which is a block?",
		"INDEF(N2) block(N2) which(N2)",
		1],	
	["Which is a green block?",
		"INDEF(N2) block(N2) green(N2) which(N2)",
		1],	
	["Where is the red block?",
		"",
	1],
	# Describe this object? (with pointing)
	["Describe this object.",
	 "DEF(N3) HEARER(N4) describe(N4,N3) object(N3)",
		1],	
	["Describe this.",
		"HEARER(N4) describe(N4,N3) this(N3)",
		1],	
	["Describe this block.",
		"DEF(N3) HEARER(N4) block(N3) describe(N4,N3)",
		1],	
	["Describe the object.",
		"DEF(N3) HEARER(N4) describe(N4,N3) object(N3)",
		1],	
	["Describe the ball.",
		"DEF(N3) HEARER(N4) ball(N3) describe(N4,N3)",
		1],	
	# What is this? (with pointing)
	["What is this?",
		"this(N2) what(N2)",
		1],	
	# What is the color of this? (with pointing)
	# What is the size of this? (with pointing)
	# What is the shape of this? (with pointing)
	["What is the color of this?",
		"DEF(N2) color(N2) of(N2,N5) this(N5) what(N2)",
		1],	
	["What color is this?",
		"",
		2],	
	["What is the color of that?",
		"DEF(N2) color(N2) of(N2,N5) that(N5) what(N2)",
		1],	
	["What is the size of this?",
	 "DEF(N2) of(N2,N5) size(N2) this(N5) what(N2)",
		1],	
	["What is the shape of this?",
		"DEF(N2) of(N2,N5) shape(N2) this(N5) what(N2)",
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
		1],	

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
		1],	

	["You are done.",
		"done(N2) you(N2)",
		2],	
	["You moved the red block to the table.",
		"DEF(N5) DEF(N6) block(N5) moved(N2,N5) red(N5) table(N6) to(moved,N6) you(N2)",
		2],
	["The action is complete.",
		"DEF(N2) action(N2) complete(N2)",
		2],	

	["No.",
		"single-word(no)",
		1],
	
	["Yes.",
		"single-word(yes)",
		1],
	
	["Is this red?",
		"",
		1],

	["Is this a block?",
		"",
		1],

	["Is the block to the right of the ball?",
		"",
		1],

	["Is the block on the table?",
		"",
		1],

	["Get a block from the pantry.",
		"DEF(N3) HEARER(N4) from(get,N3) get(N4) pantry(N3)",
		1],
	
	["Put the block to the left of the ball.",
		"DEF(N3) DEF(N4) DEF(N5) HEARER(N6) ball(N5) block(N3) left(N4) of(N4,N5) put(N6,N3) to(put,N4)",
		1],

	["Point at the red object.",
		"DEF(N4) HEARER(N5) at(point,N4) object(N4) point(N5) red(N4)",
		1],
	
	["Point at the square object.",
		"DEF(N4) HEARER(N5) at(point,N4) object(N4) point(N5) square(N4)",
		1],

	["Go to the stove.",
		"DEF(N3) HEARER(N4) go(N4) stove(N3) to(go,N3)",
		1],

	["Count the red objects.",
		"DEF(N4) HEARER(N5) count(N5,N4) objects(N4) red(N4)",
		1],

	["Count the blocks.",
		"DEF(N3) HEARER(N4) blocks(N3) count(N4,N3)",
		1],

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
	if ($simple == 0) {
		print "******** BEGIN PRIORITY $p SENTENCES ************\n";
	}
	SENTENCE:
	for ($i=0; $i<=$#sentences; $i++) {
		if ($sentences[$i][2] != $p) {
			next SENTENCE;
		}
		$sentence = $sentences[$i][0];
		$correctDRS = $sentences[$i][1];

		@output = `./run.sh "$sentence"`;

		$outDRS = `echo "@output" | ./normalizeDRS.pl`;
		chomp $outDRS;
		
		if ($outDRS eq $correctDRS and $outDRS ne "") {
			if ($simple == 0) {
				print "test sentence: $sentence\n";
				print "PASSED: $correctDRS\n";
			}
			else {
				print "$sentence PASSED\n";
			}
			$passCount++;
		}
		else {
			if ($simple == 0) {
				print "test sentence: $sentence\n";
				print "FAILED:\n";	
				print "\texpected [$correctDRS]\n";
				print "\tgot [$outDRS]\n";
#			print "Full LGSoar output:\n";
#			print @output;
			}
			else {
				print "$sentence FAILED\n";
			}

			$failCount++;
		}
		if ($simple == 0) {
			print "********************\n";
		}
	}
}

$total = $passCount + $failCount;

print "passed $passCount of $total tests.\n";
