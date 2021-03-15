package astro.celestial

import astro.utils.MathUtils

object Anomalies {
  // Astronomical functions
  // Nutation, obliquity of the ecliptic
  def nutation(context: AstroContext): Unit = { // IAU 1980 nutation theory:
    // Mean anomaly of the Moon
    val Mm = 134.962981389 + 198.867398056 * context.TE + MathUtils.trunc(477000 * context.TE) + 0.008697222222 * context.TE2 + context.TE3 / 56250D
    // Mean anomaly of the Sun
    val M = 357.527723333 + 359.05034 * context.TE + MathUtils.trunc(35640 * context.TE) - 0.0001602777778 * context.TE2 - context.TE3 / 300000D
    // Mean distance of the Moon from ascending node
    val F = 93.271910277 + 82.017538055 * context.TE + MathUtils.trunc(483120 * context.TE) - 0.0036825 * context.TE2 + context.TE3 / 327272.7273
    // Mean elongation of the Moon
    val D = 297.850363055 + 307.11148 * context.TE + MathUtils.trunc(444960 * context.TE) - 0.001914166667 * context.TE2 + context.TE3 / 189473.6842
    // Longitude of the ascending node of the Moon
    val omega = 125.044522222 - 134.136260833 * context.TE - MathUtils.trunc(1800 * context.TE) + 0.002070833333 * context.TE2 + context.TE3 / 450000D
    // Periodic terms for nutation
    val nut = Array(
      Array(0, 0, 0, 0, 1, -171996, -174.2, 92025, 8.9),
      Array(0, 0, 2, -2, 2, -13187, -1.6, 5736, -3.1),
      Array(0, 0, 2, 0, 2, -2274, -0.2, 977, -0.5),
      Array(0, 0, 0, 0, 2, 2062, 0.2, -895, 0.5),
      Array(0, -1, 0, 0, 0, -1426, 3.4, 54, -0.1),
      Array(1, 0, 0, 0, 0, 712, 0.1, -7, 0.0),
      Array(0, 1, 2, -2, 2, -517, 1.2, 224, -0.6),
      Array(0, 0, 2, 0, 1, -386, -0.4, 200, 0.0),
      Array(1, 0, 2, 0, 2, -301, 0.0, 129, -0.1),
      Array(0, -1, 2, -2, 2, 217, -0.5, -95, 0.3), Array(-1, 0, 0, 2, 0, 158, 0.0, -1, 0.0),
      Array(0, 0, 2, -2, 1, 129, 0.1, -70, 0.0), Array(-1, 0, 2, 0, 2, 123, 0.0, -53, 0.0),
      Array(1, 0, 0, 0, 1, 63, 0.1, -33, 0.0), Array(0, 0, 0, 2, 0, 63, 0.0, -2, 0.0),
      Array(-1, 0, 2, 2, 2, -59, 0.0, 26, 0.0), Array(-1, 0, 0, 0, 1, -58, -0.1, 32, 0.0),
      Array(1, 0, 2, 0, 1, -51, 0.0, 27, 0.0), Array(-2, 0, 0, 2, 0, -48, 0.0, 1, 0.0),
      Array(-2, 0, 2, 0, 1, 46, 0.0, -24, 0.0), Array(0, 0, 2, 2, 2, -38, 0.0, 16, 0.0),
      Array(2, 0, 2, 0, 2, -31, 0.0, 13, 0.0), Array(2, 0, 0, 0, 0, 29, 0.0, -1, 0.0),
      Array(1, 0, 2, -2, 2, 29, 0.0, -12, 0.0), Array(0, 0, 2, 0, 0, 26, 0.0, -1, 0.0),
      Array(0, 0, 2, -2, 0, -22, 0.0, 0, 0.0), Array(-1, 0, 2, 0, 1, 21, 0.0, -10, 0.0),
      Array(0, 2, 0, 0, 0, 17, -0.1, 0, 0.0), Array(0, 2, 2, -2, 2, -16, 0.1, 7, 0.0),
      Array(-1, 0, 0, 2, 1, 16, 0.0, -8, 0.0), Array(0, 1, 0, 0, 1, -15, 0.0, 9, 0.0),
      Array(1, 0, 0, -2, 1, -13, 0.0, 7, 0.0), Array(0, -1, 0, 0, 1, -12, 0.0, 6, 0.0),
      Array(2, 0, -2, 0, 0, 11, 0.0, 0, 0.0), Array(-1, 0, 2, 2, 1, -10, 0.0, 5, 0.0),
      Array(1, 0, 2, 2, 2, -8, 0.0, 3, 0.0), Array(0, -1, 2, 0, 2, -7, 0.0, 3, 0.0),
      Array(0, 0, 2, 2, 1, -7, 0.0, 3, 0.0), Array(1, 1, 0, -2, 0, -7, 0.0, 0, 0.0),
      Array(0, 1, 2, 0, 2, 7, 0.0, -3, 0.0), Array(-2, 0, 0, 2, 1, -6, 0.0, 3, 0.0),
      Array(0, 0, 0, 2, 1, -6, 0.0, 3, 0.0), Array(2, 0, 2, -2, 2, 6, 0.0, -3, 0.0),
      Array(1, 0, 0, 2, 0, 6, 0.0, 0, 0.0), Array(1, 0, 2, -2, 1, 6, 0.0, -3, 0.0),
      Array(0, 0, 0, -2, 1, -5, 0.0, 3, 0.0), Array(0, -1, 2, -2, 1, -5, 0.0, 3, 0.0),
      Array(2, 0, 2, 0, 1, -5, 0.0, 3, 0.0), Array(1, -1, 0, 0, 0, 5, 0.0, 0, 0.0),
      Array(1, 0, 0, -1, 0, -4, 0.0, 0, 0.0), Array(0, 0, 0, 1, 0, -4, 0.0, 0, 0.0),
      Array(0, 1, 0, -2, 0, -4, 0.0, 0, 0.0), Array(1, 0, -2, 0, 0, 4, 0.0, 0, 0.0),
      Array(2, 0, 0, -2, 1, 4, 0.0, -2, 0.0), Array(0, 1, 2, -2, 1, 4, 0.0, -2, 0.0),
      Array(1, -1, 0, -1, 0, -3, 0.0, 0, 0.0), Array(-1, -1, 2, 2, 2, -3, 0.0, 1, 0.0),
      Array(0, -1, 2, 2, 2, -3, 0.0, 1, 0.0), Array(1, -1, 2, 0, 2, -3, 0.0, 1, 0.0),
      Array(3, 0, 2, 0, 2, -3, 0.0, 1, 0.0), Array(-2, 0, 2, 0, 2, -3, 0.0, 1, 0.0),
      Array(1, 0, 2, 0, 0, 3, 0.0, 0, 0.0), Array(-1, 0, 2, 4, 2, -2, 0.0, 1, 0.0),
      Array(1, 0, 0, 0, 2, -2, 0.0, 1, 0.0), Array(-1, 0, 2, -2, 1, -2, 0.0, 1, 0.0),
      Array(0, -2, 2, -2, 1, -2, 0.0, 1, 0.0), Array(-2, 0, 0, 0, 1, -2, 0.0, 1, 0.0),
      Array(2, 0, 0, 0, 1, 2, 0.0, -1, 0.0), Array(3, 0, 0, 0, 0, 2, 0.0, 0, 0.0),
      Array(1, 1, 2, 0, 2, 2, 0.0, -1, 0.0), Array(0, 0, 2, 1, 2, 2, 0.0, -1, 0.0),
      Array(1, 0, 0, 2, 1, -1, 0.0, 0, 0.0), Array(1, 0, 2, 2, 1, -1, 0.0, 1, 0.0),
      Array(1, 1, 0, -2, 1, -1, 0.0, 0, 0.0), Array(0, 1, 0, 2, 0, -1, 0.0, 0, 0.0),
      Array(0, 1, 2, -2, 0, -1, 0.0, 0, 0.0), Array(0, 1, -2, 2, 0, -1, 0.0, 0, 0.0),
      Array(1, 0, -2, 2, 0, -1, 0.0, 0, 0.0), Array(1, 0, -2, -2, 0, -1, 0.0, 0, 0.0),
      Array(1, 0, 2, -2, 0, -1, 0.0, 0, 0.0), Array(1, 0, 0, -4, 0, -1, 0.0, 0, 0.0),
      Array(2, 0, 0, -4, 0, -1, 0.0, 0, 0.0), Array(0, 0, 2, 4, 2, -1, 0.0, 0, 0.0),
      Array(0, 0, 2, -1, 2, -1, 0.0, 0, 0.0), Array(-2, 0, 2, 4, 2, -1, 0.0, 1, 0.0),
      Array(2, 0, 2, 2, 2, -1, 0.0, 0, 0.0), Array(0, -1, 2, 0, 1, -1, 0.0, 0, 0.0),
      Array(0, 0, -2, 0, 1, -1, 0.0, 0, 0.0), Array(0, 0, 4, -2, 2, 1, 0.0, 0, 0.0),
      Array(0, 1, 0, 0, 2, 1, 0.0, 0, 0.0), Array(1, 1, 2, -2, 2, 1, 0.0, -1, 0.0),
      Array(3, 0, 2, -2, 2, 1, 0.0, 0, 0.0), Array(-2, 0, 2, 2, 2, 1, 0.0, -1, 0.0),
      Array(-1, 0, 0, 0, 2, 1, 0.0, -1, 0.0), Array(0, 0, -2, 2, 1, 1, 0.0, 0, 0.0),
      Array(0, 1, 2, 0, 1, 1, 0.0, 0, 0.0), Array(-1, 0, 4, 0, 2, 1, 0.0, 0, 0.0),
      Array(2, 1, 0, -2, 0, 1, 0.0, 0, 0.0), Array(2, 0, 0, 2, 0, 1, 0.0, 0, 0.0),
      Array(2, 0, 2, -2, 1, 1, 0.0, -1, 0.0), Array(2, 0, -2, 0, 1, 1, 0.0, 0, 0.0),
      Array(1, -1, 0, -2, 0, 1, 0.0, 0, 0.0), Array(-1, 0, 0, 1, 1, 1, 0.0, 0, 0.0),
      Array(-1, -1, 0, 2, 1, 1, 0.0, 0, 0.0), Array(0, 1, 0, 1, 0, 1, 0.0, 0, 0.0)
    )
    //Reading periodic terms
    var fMm = .0
    var fM = .0
    var fF = .0
    var fD = .0
    var f_omega = .0
    var dp:Double = 0
    var de:Double = 0
    for (x <- nut.indices) {
      fMm = nut(x)(0)
      fM = nut(x)(1)
      fF = nut(x)(2)
      fD = nut(x)(3)
      f_omega = nut(x)(4)
      dp += ((nut(x)(5) + context.TE * nut(x)(6)) * Math.sin(Math.toRadians(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)))
      de += ((nut(x)(7) + context.TE * nut(x)(8)) * Math.cos(Math.toRadians(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)))
    }
    /*
         //Corrections (Herring, 1987)
         var corr = new Array(4);
         corr[0] = " 0 0 0 0 1-725 417 213 224 ";
         corr[1] = " 0 1 0 0 0 523  61 208 -24 ";
         corr[2] = " 0 0 2-2 2 102-118 -41 -47 ";
         corr[3] = " 0 0 2 0 2 -81   0  32   0 ";
    
         x=0;
         while (x<4)
         {
            fMm = eval(corr[x].substring(0,2));
            fM = eval(corr[x].substring(2,4));
            fF = eval(corr[x].substring(4,6));
            fD = eval(corr[x].substring(6,8));
            f_omega = eval(corr[x].substring(8,10));
            dp += 0.1*(eval(corr[x].substring(10,14))*sind(fD*D+fM*M+fMm*Mm+fF*F+f_omega*omega)+eval(corr[x].substring(14,18))*cosd(fD*D+fM*M+fMm*Mm+fF*F+f_omega*omega));
            de += 0.1*(eval(corr[x].substring(18,22))*cosd(fD*D+fM*M+fMm*Mm+fF*F+f_omega*omega)+eval(corr[x].substring(22,26))*sind(fD*D+fM*M+fMm*Mm+fF*F+f_omega*omega));
            x++;
          }
          */
    // Nutation in longitude
    context.delta_psi = dp / 36000000D
    // Nutation in obliquity
    context.delta_eps = de / 36000000D
    // Mean obliquity of the ecliptic
    context.eps0 = (84381.448 - 46.815 * context.TE - 0.00059 * context.TE2 + 0.001813 * context.TE3) / 3600D
    // True obliquity of the ecliptic
    context.eps = context.eps0 + context.delta_eps
  }

  // Aberration
  def aberration(context: AstroContext): Unit = {
    context.kappa = Math.toRadians(20.49552) / 3600D
    context.pi0 = Math.toRadians(102.93735 + 1.71953 * context.TE + 0.00046 * context.TE2)
    context.e = 0.016708617 - 0.000042037 * context.TE - 0.0000001236 * context.TE2
  }
}