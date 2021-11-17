struct Inner {
	int i;
	bool b;
}

struct Outer {
	struct Inner inner;
	int i;
	bool b;
}

void voidFunc(int i, bool b) {
	ret;	
}

bool boolFunc(int i, bool b) {
	int res;
	bool bb;
	if (i == 1) {
		res = 1;
	} else {
		if (i == 2) {
			res = 2;
		} else {
			if (i == 3) {
				res = 3;
			}
		}
	}

	while (b == fls || res < 5) {
		int x;
		x = res / 2 + 4 - 8 * 16;
		if (x >= 10) {
			b = tru;
		} else {
			res = res - 1;
		}
	}

	voidFunc(res, tru);

	repeat (res * res / 2) {
		b = tru && (res <= 5) || (res > 69);
	}

	if (b != fls) {
		if (res > 15) {
			b = tru;
		}
	}

	bb = !b;
	--res;
	++res;
	res = -res;

	print << res;
	receive >> bb;

	ret res;

}

int intFunc() {
	int i;
	bool b;
	struct Outer outer;
	struct Inner inner;
	outer.inner.i = 1;
	outer.inner.b = tru;
	inner.i = 2;
	inner.b = fls;
	i = 3;
	b = tru;
	voidFunc(i, outer.inner.b);
	boolFunc(i, b);
	boolFunc(inner.i, inner.b);
	boolFunc(outer.inner.i, outer.inner.b);
}

void main() {
	intFunc();
}

