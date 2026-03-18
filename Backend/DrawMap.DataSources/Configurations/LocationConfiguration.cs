using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace DrawMap.DataSources.Configurations;

public class LocationConfiguration : IEntityTypeConfiguration<Location>
{
    public void Configure(EntityTypeBuilder<Location> builder)
    {
        builder.HasKey(l => l.Id);

        builder.Property(l => l.Id)
            .IsRequired();

        builder.Property(l => l.RouteId)
            .IsRequired();

        builder.HasOne<Route>()
            .WithMany()
            .HasForeignKey(l => l.RouteId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}