struct RealStruct{
	int r;
}


void main() {

	/// multiply declared struct
	struct RealStruct struct1;
	struct RealStruct struct1;

	/// invalid name of struct type
	struct FakeStruct struct2;

	/// non funciton declared void
	void p;

	/// should throw multiply declared
	/// should throw non-function declared void
	int j;
	void j;

	/// Multiply declared
	int i;
	int i;
	bool b;
	bool b;


	/// Undeclared
	t = 12;

	/// Dot-access of non struct type
	i.t = 12;

	/// RHS invalid struct field name
	struct1.i = 12;


}
