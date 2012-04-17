#!/usr/bin/perl

if ($ARGV[0] =~ /"--simple"/) {
	$simple = 1;
}
else {
	$simple = 0;
}

# sentences structure:
# sentence, correct structure 

@sentences = (
["What is the color of this?",
"relation-question 
  question-word what
  relation 
    p1-object 
      specifier this
    p2-object 
      specifier DEF
      word UNKNOWN
    word color-of"],
["This is a red block.",
"object-message 
  object 
    specifier this
    word red
    word block"],
["This is a yellow block.",
"object-message 
  object 
    specifier this
    word yellow
    word block"],
["This is an orange block.",
"object-message 
  object 
    specifier this
    word orange
    word block"],
["What is the shape of this?",
"relation-question 
  question-word what
  relation 
    p1-object 
      specifier this
    p2-object 
      specifier DEF
      word UNKNOWN
    word shape-of"],
["This is a square block.",
"object-message 
  object 
    specifier this
    word square
    word block"],
["This is a triangular block.",
"object-message 
  object 
    specifier this
    word triangular
    word block"],
["Point at the square block.",
"verb-command 
  verb 
    preposition 
      object 
        specifier DEF
        word square
        word block
      word at
    word point"],
["Point at the square red block.",
"verb-command 
  verb 
    preposition 
      object 
        specifier DEF
        word red
        word square
        word block
      word at
    word point"],
["Point at the yellow one.",
"verb-command 
  verb 
    preposition 
      object 
        specifier DEF
        word yellow
        word one
      word at
    word point"],
["Point at the triangular one.",
"verb-command 
  verb 
    preposition 
      object 
        specifier DEF
        word triangular
        word one
      word at
    word point"],
["Describe the blocks.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word blocks
    word describe"],
["What is to the left of the red block?",
"relation-question 
  question-word what
  relation 
    p1-object 
      specifier none
      word UNKNOWN
    p2-object 
      specifier DEF
      word red
      word block
    word left-of"],
["The orange block is to the left of the red block",
"relation-message 
  relation 
    p1-object 
      specifier DEF
      word block
      word orange
    p2-object 
      specifier DEF
      word red
      word block
    word left-of"],
["The orange block is on the table.",
"relation-message 
  relation 
    p1-object 
      specifier DEF
      word block
      word orange
    p2-object 
      specifier DEF
      word table
    word on"],
["Where is the orange block?",
"object-question 
  object 
    specifier DEF
    word orange
    word block
  question-word where"],
["Where is the red block?",
"object-question 
  object 
    specifier DEF
    word red
    word block
  question-word where"],
["Move the yellow block to the pantry.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word block
      word yellow
    preposition 
      object 
        specifier DEF
        word pantry
      word to
    word move"],
["Pick up the yellow block.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word block
      word yellow
    word pick"],
["Put the yellow block on the pantry.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word block
      word yellow
    preposition 
      object 
        specifier DEF
        word pantry
      word on
    word put"],
["You moved the yellow block to the pantry.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word block
      word yellow
    preposition 
      object 
        specifier DEF
        word pantry
      word to
    word moved"],
["Where is the yellow block?",
"object-question 
  object 
    specifier DEF
    word yellow
    word block
  question-word where"],
["Move the red block to the sink.",
"verb-command 
  verb 
    direct-object 
      specifier DEF
      word block
      word red
    preposition 
      object 
        specifier DEF
        word sink
      word to
    word move"],
["No.",
"single-word-response 
  response no"],

);

$passCount = 0;
$failCount = 0;

for ($i=0; $i<=$#sentences; $i++) {
	$sentence = $sentences[$i][0];
	$correctMessage = $sentences[$i][1];

	$outMessage = `./runMessageInterpretation.pl "$sentence" | ./extractMessage.pl`;

	chomp $outMessage;
	
	if ($outMessage eq $correctMessage and $outMessage ne "") {
		if ($simple == 0) {
			print "test sentence: $sentence\n";
			print "PASSED:\n$correctMessage\n";
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
			print "expected \n$correctMessage\n";
			print "got \n$outMessage\n";
		}
		else {
			print "$sentence FAILED\n";
		}

		$failCount++;
	}
}

$total = $passCount + $failCount;

print "passed $passCount of $total tests.\n";
