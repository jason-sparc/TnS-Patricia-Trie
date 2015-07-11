
Tracks and Switches, Patricia Trie based Map
--------------------------------------------
A flexible [patricia trie](http://en.wikipedia.org/wiki/Radix_tree) implementation for Java offering rich features with prefix-related operations. It implements the [NavigableMap](http://docs.oracle.com/javase/6/docs/api/java/util/NavigableMap.html) interface and can use any kind of object for keys as long as they can be compared bitwise via a specialized `BitwiseComparator`. It has relative performance in comparison with [TreeMap](http://docs.oracle.com/javase/6/docs/api/java/util/TreeMap.html), has as good memory footprint, and can be used as a complete replacement.

This project was inspired by an existing patricia trie implementation by Roger Kapsi and Sam Berlin at <https://github.com/rkapsi/patricia-trie>; hence, there is a similar XOR metric nearness query operation.

However, for efficiency and performance reasons, it uses a very different algorithm tailored for ease-of-use, offers more flexibility, and leaves room for more query operations. Making it more comparable to TreeMap.


Tree Design
-----------
The algorithm used is very different than most conventional patricia trie implementations. As the designer and creator, I call it the **Tracks and Switches**&trade; algorithm, because nodes are treated as tracks with many alternative bit routes called switches:

	--=
	  0
	  0------=1
	  1       0
	  0--=1   0-------=1
	  0   0   0        0----=1
	          +-=--=   0     0
	            1  0   +-=   0
	            0  1     0
	            0  0     1

> Note that all middle switches are 1-bits (and appears only in the middle of the track). A 0-bit switch can only appear in the end of a switch list with an index set to the end of the track. Also, only 1 middle switch at a time can be assigned to an index, whereas the end of the track can have 2, a 1-bit and a 0-bit switch. The end switches are also referred to as edge switches in the source code. In comparison with a node in a basic patricia trie implementation, the many 1-bit switches are the right child nodes and the 0-bit switch at the end is the left.

&hellip;

Unless there is demand, **a complete documentation** of the algorithm, why the algorithm, and the source **is not yet available, for now.** Well I guess, you could study the diagram above with the source code to get an idea of how internal things works.

--------------------------------------------
I am also working on a much efficient (re)implementation of everything, I'll make it available when it's ready.
