using DrawMap.Domain;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace DrawMap.DataSources.Configurations;

public class RouteConfiguration : IEntityTypeConfiguration<Route>
{
    public void Configure(EntityTypeBuilder<Route> builder)
    {
        builder.HasKey(r => r.Id);

        builder.Property(r => r.Id)
            .IsRequired();

        builder.HasMany(r => r.Locations)
            .WithOne()
            .OnDelete(DeleteBehavior.Cascade);
    }
}