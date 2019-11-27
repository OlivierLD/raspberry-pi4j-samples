package calc;

// Referenced classes of package astro.calc:
//      Point

public final class GreatCircleWayPoint {
	private GreatCirclePoint p;
	private Double z;

	public GreatCircleWayPoint(GreatCirclePoint p, Double z) {
		this.p = p;
		this.z = z;
	}

	public GreatCirclePoint getPoint() {
		return p;
	}

	public Double getZ() {
		return z;
	}
}
