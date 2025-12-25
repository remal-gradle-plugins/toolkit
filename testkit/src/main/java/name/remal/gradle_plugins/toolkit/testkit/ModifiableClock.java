package name.remal.gradle_plugins.toolkit.testkit;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.Nullable;

public final class ModifiableClock extends Clock {

    public static ModifiableClock modifiableClock() {
        return new ModifiableClock();
    }


    private static final Instant DEFAULT_INSTANT = Instant.parse("2000-01-01T00:00:00Z");

    private final AtomicReference<Instant> instantRef;
    private final ZoneId zoneId;

    private ModifiableClock(AtomicReference<Instant> instantRef, ZoneId zoneId) {
        super();
        this.instantRef = instantRef;
        this.zoneId = zoneId;
    }

    private ModifiableClock() {
        this(new AtomicReference<>(DEFAULT_INSTANT), UTC);
    }


    public void setInstant(Instant instant) {
        requireNonNull(instant, "instant");
        instantRef.set(instant);
    }

    public void tick(Duration duration) {
        instantRef.updateAndGet(instant -> instant.plus(duration));
    }


    @Override
    public Instant instant() {
        return instantRef.get();
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        requireNonNull(zone, "zone");
        if (zoneId.equals(zone)) {
            return this;
        }
        return new ModifiableClock();
    }


    @Override
    public String toString() {
        return format("%s[%s,%s]", getClass().getSimpleName(), instantRef.get(), zoneId);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ModifiableClock)) {
            return false;
        }
        ModifiableClock that = (ModifiableClock) object;
        return Objects.equals(instantRef.get(), that.instantRef.get())
            && Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return hash(instantRef.get(), zoneId);
    }

}
