#!/usr/bin/perl

@sentences = (
#	["2.1.1,1", "go to the storage unit."],
#	["2.1.1,2a", "put the blue ball on the table."],
#	["2.1.1,2b", "put the blue ball on top of the table."],
#	["2.1.1,3", "give this ball to me."],
#	["2.1.1,4", "give me the blue ball."],
#	["2.1.2*", "goal of get ball is holding ball, blue ball on the table"],
	["4.2.1a", "put the red block on the table."],
	["4.2.1b", "gently put the red block on the table."],
	["4.2.1c", "get the ball if you are not holding the ball."],
	["4.2.2", "the goal of getting the ball is to be holding the ball."],
	["4.2.3*", "you have performed \' put the red ball on the table \'."],
	["4.2.4", "the ball is now on the table."],
	["4.2.5a", "this is a red ball."],
	["4.2.5b", "the color of this ball is red."],
	["4.2.6a", "the ball is in the cabinet."],
	["4.2.6b", "a plate is a dish."],
	["4.2.6c", "this cup has a handle."],
	["4.2.7", "which is the blue box?"],
	["4.2.8", "what is a blue box?"],
	["4.2.9", "what is the color of this ball?"],
	["4.2.10", "the blue box."],
	["4.2.10b", "it is the blue box."],
	["4.2.11", "yes."],
	["4.2.11b", "that is correct."],
	["4.2.12", "the color red."],
	["4.2.13", "the attribute color."],
	["?", "describe this object."]


);

for ($i=0; $i<=$#sentences; $i++) {
	$section = $sentences[$i][0];
	$sentence = $sentences[$i][1];

	print "***\n";
	print "test sentence $section: $sentence\n";
	print `./handleSentence.pl $sentence`;
}
