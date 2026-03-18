using DrawMap.Domain;
using DrawMap.Repositories;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Services;

public class RouteService : IRouteService
{
    private readonly IRouteRepository _routeRepository;

    public RouteService(IRouteRepository routeRepository)
    {
        _routeRepository = routeRepository;
    }

    public async Task<DomainRoute> AddRoute(DomainRoute route, CancellationToken cancellationToken)
    {
        route.Id = Guid.NewGuid().ToString();
        route.Locations ??= [];
        foreach (var location in route.Locations)
            location.Id ??= Guid.NewGuid().ToString();
        await _routeRepository.AddRoute(route, cancellationToken);
        return route;
    }

    public async Task<DomainRoute?> GetRoute(string routeId, CancellationToken cancellationToken)
    {
        return await _routeRepository.GetRoute(routeId, cancellationToken);
    }

    public async Task<List<DomainRoute>> GetRoutes(CancellationToken cancellationToken)
    {
        return await _routeRepository.GetRoutes(cancellationToken);
    }

    public async Task<bool> DeleteRoute(string routeId, CancellationToken cancellationToken)
    {
        return await _routeRepository.DeleteRoute(routeId, cancellationToken);
    }

    public async Task<bool> UpdateRoute(string routeId, DomainRoute route, CancellationToken cancellationToken)
    {
        var result = await _routeRepository.UpdateRoute(routeId, route, cancellationToken);
        return result is not null;
    }
}
